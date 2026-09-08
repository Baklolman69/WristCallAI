package com.wristcall.wear.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.wear.compose.material.CircularProgressIndicator
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
import java.util.Locale

/**
 * Dedicated AI Call Summary Screen tailored for Wear OS watch displays.
 * Displays short bullet points with vibrant aesthetics and voice playback.
 */
@Composable
fun SummaryScreen(
    taskPrompt: String,
    transcript: String,
    groqApiKey: String = "",
    onBackToTranscript: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var summaryText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var isPlayingVoice by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                ttsEngine?.stop()
                ttsEngine?.shutdown()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(transcript, groqApiKey) {
        isLoading = true
        errorMessage = ""
        try {
            summaryText = generateGroqBulletSummary(taskPrompt, transcript, groqApiKey)
        } catch (e: Exception) {
            errorMessage = e.message?.take(70) ?: "Summarization failed"
        } finally {
            isLoading = false
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
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Summary",
                    tint = Color(0xFF6EE7B7),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Call Summary",
                    style = MaterialTheme.typography.caption1,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Task prompt chip
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1B4B))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "📞 ${taskPrompt.take(50)}",
                    fontSize = 9.sp,
                    color = Color(0xFFC7D2FE),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { Spacer(modifier = Modifier.height(2.dp)) }

        // Content / Loading / Error
        if (isLoading) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        indicatorColor = Color(0xFF818CF8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Groq AI 120B Summarizing...",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (errorMessage.isNotBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF7F1D1D))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "⚠ $errorMessage",
                        fontSize = 10.sp,
                        color = Color(0xFFFCA5A5),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Parse bullets
            val bullets = summaryText.lines().map { it.trim() }.filter { it.isNotBlank() }
            items(bullets.size) { index ->
                val line = bullets[index]
                val bulletText = if (line.startsWith("•") || line.startsWith("-")) line else "• $line"
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF064E3B))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = bulletText,
                        fontSize = 10.5.sp,
                        color = Color(0xFFD1FAE5),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(2.dp)) }

            // Voice Readout Button
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (isPlayingVoice) {
                            ttsEngine?.stop()
                            isPlayingVoice = false
                        } else {
                            isPlayingVoice = true
                            var tts: TextToSpeech? = null
                            tts = TextToSpeech(context) { status ->
                                if (status == TextToSpeech.SUCCESS) {
                                    tts?.language = Locale.US
                                    tts?.speak(summaryText.take(400), TextToSpeech.QUEUE_FLUSH, null, "summary_voice")
                                    ttsEngine = tts
                                }
                            }
                        }
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (isPlayingVoice) Color(0xFFD97706) else Color(0xFF059669)
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Voice",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isPlayingVoice) "⏸ Stop Voice" else "🔊 Listen Summary",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Back to Transcript Button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBackToTranscript,
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF312E81)),
                icon = {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = "Transcript",
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(15.dp)
                    )
                },
                label = {
                    Text(
                        text = "Full Transcript",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE0E7FF)
                    )
                }
            )
        }

        // Home Button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onHome,
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                },
                label = {
                    Text(
                        text = "Back to Home",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                }
            )
        }
    }
}

private suspend fun generateGroqBulletSummary(taskPrompt: String, transcript: String, groqApiKey: String): String = withContext(Dispatchers.IO) {
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

            val systemPrompt = "You are an AI assistant on a smartwatch. Summarize phone calls in exactly 3 short bullet points starting with '• '. Keep each bullet under 8 words so it fits perfectly on small watch screens."
            val userPrompt = "Task: $taskPrompt\n\nTranscript:\n$transcript\n\nProvide 3 short bullet points summarizing the call result."

            val bodyJson = JSONObject().apply {
                put("model", modelName)
                put("temperature", 0.2)
                put("max_tokens", 250)
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
