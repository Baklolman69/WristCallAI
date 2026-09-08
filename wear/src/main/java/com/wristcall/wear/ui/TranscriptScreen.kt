package com.wristcall.wear.ui

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Wear OS Transcript Screen — shows call conversation log, audio playback, and Groq AI summarization.
 */
@Composable
fun TranscriptScreen(
    transcript: String,
    taskPrompt: String,
    recordingUrl: String = "",
    groqApiKey: String = "",
    onSummarize: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var aiSummary by remember { mutableStateOf("") }
    var isSummarizing by remember { mutableStateOf(false) }
    var summaryError by remember { mutableStateOf("") }

    var isPlayingAudio by remember { mutableStateOf(false) }
    var isAudioBuffering by remember { mutableStateOf(false) }
    var audioError by remember { mutableStateOf("") }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) it.stop()
                    it.release()
                }
                ttsEngine?.shutdown()
            } catch (_: Exception) {}
        }
    }

    // Parse transcript into turns
    val turns = remember(transcript) {
        transcript.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals("Not Found", ignoreCase = true) && !it.startsWith("CALL-E API Error", ignoreCase = true) }
            .map { line ->
                when {
                    line.startsWith("[AGENT]:", ignoreCase = true) ->
                        "AGENT" to line.substringAfter(":").trim()
                    line.startsWith("[RECIPIENT]:", ignoreCase = true) ->
                        "RECIPIENT" to line.substringAfter(":").trim()
                    line.startsWith("[BOT]:", ignoreCase = true) ->
                        "AGENT" to line.substringAfter(":").trim()
                    line.startsWith("[USER]:", ignoreCase = true) ->
                        "RECIPIENT" to line.substringAfter(":").trim()
                    line.contains("speaker: bot", ignoreCase = true) ||
                    line.contains("speaker:bot", ignoreCase = true) ->
                        "AGENT" to line.substringAfter("text:").trim()
                    line.contains("speaker: user", ignoreCase = true) ||
                    line.contains("speaker:user", ignoreCase = true) ->
                        "RECIPIENT" to line.substringAfter("text:").trim()
                    else -> "SYSTEM" to line
                }
            }
    }

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Transcript",
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Call Transcript",
                    style = MaterialTheme.typography.caption1,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Task prompt badge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1B4B))
                    .padding(8.dp)
            ) {
                Text(
                    text = "📞 ${taskPrompt.take(60)}",
                    fontSize = 9.sp,
                    color = Color(0xFFC7D2FE),
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(modifier = Modifier.height(2.dp)) }

        // Transcript turns as chat bubbles
        if (turns.isEmpty()) {
            item {
                Text(
                    text = "No transcript available",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(turns.size) { index ->
                val (speaker, text) = turns[index]
                if (text.isNotBlank()) {
                    TranscriptBubble(speaker = speaker, text = text)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Listen Call Recording button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    audioError = ""
                    if (isPlayingAudio) {
                        try {
                            mediaPlayer?.let {
                                if (it.isPlaying) it.pause()
                            }
                            ttsEngine?.stop()
                        } catch (_: Exception) {}
                        isPlayingAudio = false
                    } else {
                        if (recordingUrl.isNotBlank()) {
                            isAudioBuffering = true
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val player = MediaPlayer().apply {
                                        setDataSource(recordingUrl)
                                        prepare()
                                        setOnCompletionListener {
                                            isPlayingAudio = false
                                        }
                                    }
                                    mediaPlayer = player
                                    withContext(Dispatchers.Main) {
                                        isAudioBuffering = false
                                        player.start()
                                        isPlayingAudio = true
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isAudioBuffering = false
                                        audioError = "Stream error. Playing speech audio..."
                                        playTranscriptTTS(context, transcript) { engine ->
                                            ttsEngine = engine
                                            isPlayingAudio = true
                                        }
                                    }
                                }
                            }
                        } else {
                            isPlayingAudio = true
                            playTranscriptTTS(context, transcript) { engine ->
                                ttsEngine = engine
                            }
                        }
                    }
                },
                colors = ChipDefaults.chipColors(
                    backgroundColor = if (isPlayingAudio) Color(0xFFD97706) else Color(0xFF10B981)
                ),
                icon = {
                    Icon(
                        imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.VolumeUp,
                        contentDescription = "Audio Recording",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = when {
                            isAudioBuffering -> "Buffering Audio..."
                            isPlayingAudio -> "⏸ Pause Recording"
                            else -> "🔊 Listen Recording"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        }

        if (audioError.isNotBlank()) {
            item {
                Text(
                    text = "ℹ $audioError",
                    fontSize = 9.sp,
                    color = Color(0xFFFBBF24),
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Summarize with AI button (opens dedicated bullet summary page)
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSummarize,
                colors = ChipDefaults.chipColors(
                    backgroundColor = Color(0xFF6366F1)
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Summarize",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = "✨ Summarize with AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        }

        // AI Summary display
        if (aiSummary.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF064E3B))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color(0xFF6EE7B7),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Summary (Groq)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6EE7B7)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = aiSummary,
                        fontSize = 10.sp,
                        color = Color(0xFFD1FAE5),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Error display
        if (summaryError.isNotBlank()) {
            item {
                Text(
                    text = "⚠ $summaryError",
                    fontSize = 10.sp,
                    color = Color(0xFFF87171),
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Done button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss,
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = "Back to Home",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                }
            )
        }
    }
}

@Composable
private fun TranscriptBubble(speaker: String, text: String) {
    val isAgent = speaker == "AGENT"
    val isSystem = speaker == "SYSTEM"

    val bgColor = when {
        isAgent -> Color(0xFF312E81)
        isSystem -> Color(0xFF1E293B)
        else -> Color(0xFF064E3B)
    }
    val labelColor = when {
        isAgent -> Color(0xFF818CF8)
        isSystem -> Color(0xFF94A3B8)
        else -> Color(0xFF34D399)
    }
    val label = when {
        isAgent -> "🤖 CALL-E"
        isSystem -> "ℹ System"
        else -> "👤 Recipient"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isAgent) 0.dp else 12.dp,
                end = if (isAgent) 12.dp else 0.dp
            )
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
        Spacer(modifier = Modifier.height(1.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * Calls Groq API using native HttpURLConnection to summarize the call transcript.
 */
private suspend fun summarizeWithGroq(transcript: String, taskPrompt: String, groqApiKey: String): String = withContext(Dispatchers.IO) {
    if (groqApiKey.isBlank()) {
        return@withContext "• Task: $taskPrompt\n• Call completed\n• Groq AI Key not configured in Settings"
    }
    val modelsToTry = listOf("gpt-oss-120b", "openai/gpt-oss-120b", "llama-3.3-70b-versatile", "llama-3.1-8b-instant")
    var lastException: Exception? = null

    for (modelName in modelsToTry) {
        try {
            val url = URL("https://api.groq.com/openai/v1/chat/completions")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $groqApiKey")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val systemPrompt = "You are an AI assistant summarizing phone calls for a smartwatch. Give a concise 2-3 sentence summary of what happened in the call, including purpose and outcome."
            val userPrompt = "Task: $taskPrompt\n\nTranscript:\n$transcript\n\nSummarize this call in 2-3 short sentences."

            val bodyJson = JSONObject().apply {
                put("model", modelName)
                put("temperature", 0.3)
                put("max_tokens", 300)
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                }
                put("messages", messages)
            }

            connection.outputStream.use { os ->
                os.write(bodyJson.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val choices = json.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    return@withContext message.getString("content").trim()
                }
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                lastException = Exception("Groq API error ($responseCode): ${errorText.take(60)}")
            }
        } catch (e: Exception) {
            lastException = e
        }
    }

    throw lastException ?: Exception("Groq AI Summarization failed")
}

private fun playTranscriptTTS(
    context: android.content.Context,
    transcript: String,
    onReady: (TextToSpeech) -> Unit
) {
    var tts: TextToSpeech? = null
    tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = java.util.Locale.US
            val speechText = transcript.lines()
                .filter { it.contains(":") }
                .joinToString(". ") { line ->
                    line.replace("[AGENT]:", "Agent says:").replace("[RECIPIENT]:", "Recipient says:")
                }
                .ifBlank { "Call completed. Summary: $transcript" }
            tts?.speak(speechText.take(500), TextToSpeech.QUEUE_FLUSH, null, "call_transcript_audio")
            tts?.let { onReady(it) }
        }
    }
}
