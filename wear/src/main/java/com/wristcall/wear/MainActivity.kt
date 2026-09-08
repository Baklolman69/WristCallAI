package com.wristcall.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.MaterialTheme
import com.calle.sdk.CallEClient
import com.calle.sdk.data.CallEPreferences
import com.calle.sdk.data.SmartCallResolver
import com.calle.sdk.models.CallEStatus
import com.calle.sdk.models.CallRequest
import com.wristcall.wear.ui.ActiveCallScreen
import com.wristcall.wear.ui.ApiKeySetupScreen
import com.wristcall.wear.ui.SummaryScreen
import com.wristcall.wear.ui.TaskResultScreen
import com.wristcall.wear.ui.TranscriptScreen
import com.wristcall.wear.ui.VoiceInputScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import com.wristcall.wear.ui.SerpApiSetupScreen

import com.wristcall.wear.ui.SettingsScreen

import android.content.Intent
import android.view.KeyEvent

enum class WearScreenState {
    API_KEY_SETUP,
    SERP_API_SETUP,
    SETTINGS,
    VOICE_INPUT,
    ACTIVE_CALL,
    TASK_RESULT,
    TRANSCRIPT,
    SUMMARY
}

class MainActivity : ComponentActivity() {
    private var lastKeyPressTime = 0L
    private val autoStartListeningState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAutoListenIntent(intent)
        setContent {
            MaterialTheme {
                WristCallWearApp(autoStartListening = autoStartListeningState.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkAutoListenIntent(intent)
    }

    private fun checkAutoListenIntent(intent: Intent?) {
        if (intent == null) {
            autoStartListeningState.value = false
            return
        }
        val shouldAutoListen = intent.action == Intent.ACTION_VOICE_COMMAND ||
            intent.getBooleanExtra("AUTO_LISTEN", false) ||
            intent.getBooleanExtra("START_MIC", false) ||
            intent.hasExtra("DOUBLE_CLICK")
        autoStartListeningState.value = shouldAutoListen
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentTime = System.currentTimeMillis()
        if (keyCode == KeyEvent.KEYCODE_STEM_PRIMARY ||
            keyCode == KeyEvent.KEYCODE_STEM_1 ||
            keyCode == KeyEvent.KEYCODE_STEM_2 ||
            keyCode == KeyEvent.KEYCODE_NAVIGATE_NEXT ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            if (currentTime - lastKeyPressTime in 50..650) {
                // Double click hardware Wear OS button detected -> Open mic & start listening immediately!
                autoStartListeningState.value = true
                return true
            }
            lastKeyPressTime = currentTime
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun WristCallWearApp(autoStartListening: Boolean = false) {
    val context = LocalContext.current
    val prefs = remember { CallEPreferences(context) }

    var apiKey by remember { mutableStateOf(prefs.apiKey) }
    var screenState by remember {
        mutableStateOf(if (apiKey.isBlank()) WearScreenState.API_KEY_SETUP else WearScreenState.VOICE_INPUT)
    }

    var currentPrompt by remember { mutableStateOf("") }
    var currentTarget by remember { mutableStateOf("Joe's Diner") }
    var callStatus by remember { mutableStateOf(CallEStatus.READY) }
    var resultSuccess by remember { mutableStateOf(true) }
    var resultTitle by remember { mutableStateOf("") }
    var resultDetails by remember { mutableStateOf("") }
    var callTranscript by remember { mutableStateOf("") }
    var recordingUrl by remember { mutableStateOf("") }

    var liveStatusText by remember { mutableStateOf("Connecting to CALL-E AI...") }
    var liveSeconds by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val client = remember(apiKey) { CallEClient(apiKey = apiKey.ifBlank { "DEMO_KEY" }) }

    when (screenState) {
        WearScreenState.API_KEY_SETUP -> {
            ApiKeySetupScreen(
                onApiKeySaved = { newKey ->
                    prefs.apiKey = newKey
                    apiKey = newKey
                    screenState = WearScreenState.SERP_API_SETUP
                }
            )
        }

        WearScreenState.SERP_API_SETUP -> {
            SerpApiSetupScreen(
                currentSerpKey = prefs.serpApiKey,
                onSerpKeySaved = { key ->
                    if (key.isNotBlank()) prefs.serpApiKey = key
                    screenState = WearScreenState.VOICE_INPUT
                },
                onSkip = {
                    screenState = WearScreenState.VOICE_INPUT
                }
            )
        }

        WearScreenState.SETTINGS -> {
            SettingsScreen(
                onSaveCompleted = { newCallEKey, newSerpKey, newGroqKey ->
                    prefs.apiKey = newCallEKey
                    prefs.serpApiKey = newSerpKey
                    prefs.groqApiKey = newGroqKey
                    apiKey = newCallEKey
                    screenState = WearScreenState.VOICE_INPUT
                },
                onBack = {
                    screenState = WearScreenState.VOICE_INPUT
                }
            )
        }

        WearScreenState.VOICE_INPUT -> {
            VoiceInputScreen(
                apiKey = apiKey,
                onResetApiKey = {
                    screenState = WearScreenState.API_KEY_SETUP
                },
                onOpenSettings = {
                    screenState = WearScreenState.SETTINGS
                },
                autoStartListening = autoStartListening,
                onTaskSelected = { prompt, targetPhone, category ->
                    currentPrompt = prompt
                    currentTarget = parseTargetNameFromPrompt(prompt, targetPhone)
                    callStatus = CallEStatus.DISPATCHING
                    callTranscript = ""
                    liveSeconds = 0
                    liveStatusText = "1/5 Searching Google via SerpApi..."
                    screenState = WearScreenState.ACTIVE_CALL

                    coroutineScope.launch {
                        val timerJob = launch {
                            var s = 0
                            while (isActive) {
                                delay(1000)
                                s++
                                liveSeconds = s
                            }
                        }

                        try {
                            // Step 1 & 2: Discover phone & refine intent via SerpApi + Groq AI
                            val resolver = SmartCallResolver(
                                serpApiKey = prefs.serpApiKey,
                                groqApiKey = prefs.groqApiKey
                            )
                            val resolvedInfo = resolver.resolveCall(
                                rawPrompt = prompt,
                                providedPhone = targetPhone,
                                onStatusUpdate = { status -> liveStatusText = status }
                            )

                            if (resolvedInfo.businessName.isNotBlank()) {
                                currentTarget = resolvedInfo.businessName
                            }

                            // Step 3: Dispatch to CALL-E telecom network
                            liveStatusText = "3/5 Dispatching AI Call..."
                            val request = CallRequest(
                                toPhoneNumber = resolvedInfo.phone,
                                promptInstructions = resolvedInfo.refinedPrompt,
                                taskCategory = category
                            )
                            val dispatchResult = client.dispatchCall(request)

                            if (dispatchResult.isSuccess) {
                                val callResponse = dispatchResult.getOrNull()
                                val callId = callResponse?.callId ?: ""

                                if (client.isDemoMode) {
                                    liveStatusText = "4/5 Simulating Agent Call..."
                                    callStatus = CallEStatus.CALL_IN_PROGRESS
                                    delay(2200)
                                    val transcriptResult = client.getTranscript(callId, prompt)
                                    callTranscript = transcriptResult.getOrDefault("")
                                    callStatus = CallEStatus.SUCCESS
                                    resultSuccess = true
                                    resultTitle = "Call Completed"
                                    resultDetails = callResponse?.summary ?: "Task: \"${prompt.take(40)}\""
                                    screenState = WearScreenState.TRANSCRIPT
                                } else {
                                    // Real CALL-E call: poll live call status from CALL-E API
                                    callStatus = CallEStatus.CALL_IN_PROGRESS
                                    liveStatusText = "4/5 Dialing $currentTarget..."
                                    var pollCount = 0
                                    var finalTranscript = ""
                                    var finalSummary = ""
                                    var isCompleted = false

                                    while (pollCount < 25 && !isCompleted) {
                                        delay(3000)
                                        pollCount++

                                        val statusResult = client.getCallStatus(callId)
                                        if (statusResult.isSuccess) {
                                            val statusObj = statusResult.getOrThrow()
                                            val stateStr = statusObj.status.lowercase()

                                            liveStatusText = when {
                                                stateStr.contains("queue") -> "2/4 Queued on Telecom Network..."
                                                stateStr.contains("dial") || stateStr.contains("ring") -> "2/4 Ringing $currentTarget..."
                                                stateStr.contains("progress") || stateStr.contains("talk") || stateStr.contains("active") -> "3/4 AI Agent in Conversation..."
                                                stateStr.contains("summary") || stateStr.contains("complete") || stateStr.contains("finished") -> "4/4 Extracting Transcript..."
                                                else -> "Status: ${statusObj.status}"
                                            }

                                            val turns = statusObj.allTranscriptTurns
                                            if (!turns.isNullOrEmpty()) {
                                                finalTranscript = turns.joinToString("\n") { turn ->
                                                    val speakerLabel = if (turn.speaker.equals("bot", ignoreCase = true) || turn.speaker.equals("agent", ignoreCase = true)) "[AGENT]" else "[RECIPIENT]"
                                                    "$speakerLabel: ${turn.text}"
                                                }
                                            }

                                            val summaryStr = statusObj.bestSummary
                                            if (!summaryStr.isNullOrBlank()) {
                                                finalSummary = summaryStr
                                            }

                                            val recUrl = statusObj.bestRecordingUrl
                                            if (!recUrl.isNullOrBlank()) {
                                                recordingUrl = recUrl
                                            }

                                            if (stateStr.contains("completed") || stateStr.contains("success") || stateStr.contains("finished") || stateStr.contains("failed") || stateStr.contains("error") || !turns.isNullOrEmpty()) {
                                                isCompleted = true
                                            }
                                        }
                                    }

                                    // Polling complete or timeout reached — fetch final transcript
                                    liveStatusText = "4/4 Finalizing Transcript..."
                                    val transcriptResult = client.getTranscript(callId, prompt)
                                    callTranscript = when {
                                        finalTranscript.isNotBlank() -> finalTranscript
                                        finalSummary.isNotBlank() -> "[AGENT]: Task Instructions: \"$prompt\"\n[RECIPIENT]: $finalSummary"
                                        else -> transcriptResult.getOrDefault("")
                                    }

                                    callStatus = CallEStatus.SUCCESS
                                    resultSuccess = true
                                    resultTitle = "Call Completed"
                                    resultDetails = if (finalSummary.isNotBlank()) finalSummary else "Real CALL-E call executed."
                                    screenState = WearScreenState.TRANSCRIPT
                                }
                            } else {
                                val errorMsg = dispatchResult.exceptionOrNull()?.message ?: "Unknown error"
                                callStatus = CallEStatus.FAILED
                                resultSuccess = false
                                resultTitle = "Call Failed"
                                resultDetails = errorMsg.take(80)
                                screenState = WearScreenState.TASK_RESULT
                            }
                        } finally {
                            timerJob.cancel()
                        }
                    }
                }
            )
        }

        WearScreenState.ACTIVE_CALL -> {
            ActiveCallScreen(
                targetName = currentTarget,
                status = callStatus,
                liveStatusText = liveStatusText,
                durationSeconds = liveSeconds
            )
        }

        WearScreenState.TASK_RESULT -> {
            TaskResultScreen(
                isSuccess = resultSuccess,
                summaryTitle = resultTitle,
                summaryDetails = resultDetails,
                onDismiss = {
                    screenState = WearScreenState.VOICE_INPUT
                }
            )
        }

        WearScreenState.TRANSCRIPT -> {
            TranscriptScreen(
                transcript = callTranscript,
                taskPrompt = currentPrompt,
                recordingUrl = recordingUrl,
                groqApiKey = prefs.groqApiKey,
                onSummarize = {
                    screenState = WearScreenState.SUMMARY
                },
                onDismiss = {
                    screenState = WearScreenState.VOICE_INPUT
                }
            )
        }

        WearScreenState.SUMMARY -> {
            SummaryScreen(
                taskPrompt = currentPrompt,
                transcript = callTranscript,
                groqApiKey = prefs.groqApiKey,
                onBackToTranscript = {
                    screenState = WearScreenState.TRANSCRIPT
                },
                onHome = {
                    screenState = WearScreenState.VOICE_INPUT
                }
            )
        }
    }
}

private fun parseTargetNameFromPrompt(prompt: String, phone: String): String {
    return when {
        prompt.contains("Pizza Hut", ignoreCase = true) -> "Pizza Hut Dallas"
        prompt.contains("Hardware", ignoreCase = true) -> "Hardware Store"
        prompt.contains("Joe's Diner", ignoreCase = true) -> "Joe's Diner"
        prompt.startsWith("Call ", ignoreCase = true) -> {
            val afterCall = prompt.substring(5).trim()
            val targetPart = afterCall.split(" for ", " to ", " and ", " at ").firstOrNull()?.trim()
            if (!targetPart.isNullOrBlank()) targetPart else afterCall.take(25)
        }
        prompt.isNotBlank() -> prompt.take(25)
        phone.isNotBlank() -> "Target ($phone)"
        else -> "Target Contact"
    }
}

