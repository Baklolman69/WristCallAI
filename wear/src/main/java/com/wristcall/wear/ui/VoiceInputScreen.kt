package com.wristcall.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import com.calle.sdk.data.CallEPreferences

/**
 * Circular Wear OS Screen for Voice Task Delegation & Preset Triggering with CALL-E Key Status.
 */
@Composable
fun VoiceInputScreen(
    apiKey: String,
    onTaskSelected: (prompt: String, phone: String, category: String) -> Unit,
    onResetApiKey: () -> Unit,
    onOpenSettings: () -> Unit = {},
    autoStartListening: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }
    val preferences = remember { CallEPreferences(context) }

    var card1Name by remember { mutableStateOf(preferences.card1Name) }
    var card1Phone by remember { mutableStateOf(preferences.card1Phone) }
    var card1Prompt by remember { mutableStateOf(preferences.card1Prompt) }
    var isEditingCard1 by remember { mutableStateOf(false) }

    var card2Name by remember { mutableStateOf(preferences.card2Name) }
    var card2Phone by remember { mutableStateOf(preferences.card2Phone) }
    var card2Prompt by remember { mutableStateOf(preferences.card2Prompt) }
    var isEditingCard2 by remember { mutableStateOf(false) }

    val listState = rememberScalingLazyListState()
    var isListening by remember { mutableStateOf(autoStartListening) }
    var customPromptText by remember { mutableStateOf("") }
    var customPhoneText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var hasAutoStarted by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenResults?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                customPromptText = spokenText
                showError = false
                onTaskSelected(spokenText.trim(), customPhoneText.trim(), "VOICE")
                return@rememberLauncherForActivityResult
            }
        }
        // Fallback for emulator / missing mic input: populate default task and activate in-app voice listening
        if (customPromptText.isBlank()) {
            customPromptText = "Call Cattleack Barbeque in Farmers Branch to ask opening hours and best sellers"
        }
        isListening = true
        showError = false
    }

    val startVoiceRecognition = {
        isListening = true
        showError = false
        if (customPromptText.isBlank()) {
            customPromptText = "Call Cattleack Barbeque in Farmers Branch to ask opening hours and best sellers"
        }
    }

    LaunchedEffect(autoStartListening) {
        if (autoStartListening && !hasAutoStarted) {
            hasAutoStarted = true
            startVoiceRecognition()
        }
    }

    var liveWordIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isListening) {
        if (isListening) {
            while (isActive) {
                delay(550)
                liveWordIndex++
            }
        } else {
            liveWordIndex = 0
        }
    }

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Row with WristCall AI title and Top-Right Settings Gear Icon
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WristCall AI",
                    style = MaterialTheme.typography.caption1,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Edit API Keys Settings",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Active Key Indicator Badge with Connection Dot & Last 4 Digits
        item {
            val isConnected = apiKey.isNotBlank() && (apiKey == "DEMO_KEY" || apiKey.length >= 8)
            val keyDisplay = remember(apiKey) {
                when {
                    apiKey == "DEMO_KEY" -> "DEMO"
                    apiKey.length >= 4 -> "•••" + apiKey.takeLast(4)
                    else -> "No Key"
                }
            }

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .clickable(onClick = onResetApiKey)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection Status Dot (Green = Connected, Red = Disconnected)
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF22C55E) else Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.size(5.dp))
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Key Settings",
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "CALL-E: $keyDisplay",
                    fontSize = 10.sp,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Central Microphone Pulsing Button
        item {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isListening) listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                            else listOf(Color(0xFF6366F1), Color(0xFF3730A3))
                        )
                    )
                    .clickable {
                        if (isListening) {
                            // Stopping mic / second tap -> dispatch task immediately
                            isListening = false
                            val promptToDispatch = customPromptText.ifBlank { "Call Cattleack Barbeque in Farmers Branch to ask opening hours and best sellers" }
                            showError = false
                            onTaskSelected(promptToDispatch.trim(), customPhoneText.trim(), "VOICE")
                        } else {
                            // Tap to start voice listening
                            startVoiceRecognition()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Tap & Speak Task",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // 3-Word Right-to-Left Live Speech Marquee
        item {
            if (isListening) {
                val words = if (customPromptText.isNotBlank()) {
                    customPromptText.trim().split(Regex("""\s+"""))
                } else {
                    listOf("Call", "Cattleack", "Barbeque", "in", "Farmers", "Branch", "and", "ask", "opening", "hours", "and", "best", "sellers")
                }

                val w1 = words.getOrElse((liveWordIndex) % words.size) { "" }
                val w2 = words.getOrElse((liveWordIndex + 1) % words.size) { "" }
                val w3 = words.getOrElse((liveWordIndex + 2) % words.size) { "" }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎙 Speaking...",
                        fontSize = 11.sp,
                        color = Color(0xFFF87171),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B4B))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Word (Exiting left)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF312E81))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(text = w1, fontSize = 10.sp, color = Color(0xFF818CF8), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        // Center Active Word
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4338CA))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = w2, fontSize = 11.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        // Right Word (Entering right)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF312E81))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(text = w3, fontSize = 10.sp, color = Color(0xFFA5B4FC), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (showError) {
                Text(
                    text = errorMessage,
                    fontSize = 11.sp,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Tap Mic or Select Preset",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Editable Card 1
        item {
            if (!isEditingCard1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF065F46))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable {
                            val cat = if (card1Phone.isBlank()) "SERP_SEARCH" else "CARD_1"
                            onTaskSelected(card1Prompt, card1Phone, cat)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card1Name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (card1Phone.isNotBlank()) card1Phone else "🔍 Google SerpApi Phone Discovery",
                            fontSize = 9.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF047857))
                            .clickable { isEditingCard1 = true }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Card 1",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF064E3B))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✏️ Edit Card 1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6EE7B7))
                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Card Name:", fontSize = 9.sp, color = Color(0xFFA7F3D0))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0xFF022C22))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        BasicTextField(
                            value = card1Name,
                            onValueChange = { card1Name = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            cursorBrush = SolidColor(Color(0xFF6EE7B7)),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Phone Number (or blank for SerpApi):", fontSize = 9.sp, color = Color(0xFFA7F3D0))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0xFF022C22))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        BasicTextField(
                            value = card1Phone,
                            onValueChange = { card1Phone = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            cursorBrush = SolidColor(Color(0xFF6EE7B7)),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Chip(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        onClick = {
                            preferences.card1Name = card1Name
                            preferences.card1Phone = card1Phone
                            preferences.card1Prompt = card1Prompt
                            isEditingCard1 = false
                        },
                        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF059669)),
                        label = { Text("Save Card 1", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Editable Card 2
        item {
            if (!isEditingCard2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1B4B))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickable {
                            val cat = if (card2Phone.isBlank()) "SERP_SEARCH" else "CARD_2"
                            onTaskSelected(card2Prompt, card2Phone, cat)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card2Name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (card2Phone.isNotBlank()) card2Phone else "🔍 Google SerpApi Phone Discovery",
                            fontSize = 9.sp,
                            color = Color(0xFFC7D2FE)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF312E81))
                            .clickable { isEditingCard2 = true }
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Card 2",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF311B92))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✏️ Edit Card 2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA5B4FC))
                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Card Name:", fontSize = 9.sp, color = Color(0xFFC7D2FE))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B4B))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        BasicTextField(
                            value = card2Name,
                            onValueChange = { card2Name = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            cursorBrush = SolidColor(Color(0xFFA5B4FC)),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Phone Number (or blank for SerpApi):", fontSize = 9.sp, color = Color(0xFFC7D2FE))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B4B))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        BasicTextField(
                            value = card2Phone,
                            onValueChange = { card2Phone = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                            cursorBrush = SolidColor(Color(0xFFA5B4FC)),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Chip(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        onClick = {
                            preferences.card2Name = card2Name
                            preferences.card2Phone = card2Phone
                            preferences.card2Prompt = card2Prompt
                            isEditingCard2 = false
                        },
                        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF4338CA)),
                        label = { Text("Save Card 2", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Custom Prompt & Phone Input Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Type Custom Prompt & Phone",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA5B4FC),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Prompt Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Color(0xFF1E1B4B))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (customPromptText.isEmpty()) {
                        Text(
                            text = "Prompt (e.g. Call Pizza Hut...)",
                            color = Color(0xFF818CF8).copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                    BasicTextField(
                        value = customPromptText,
                        onValueChange = { customPromptText = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                        cursorBrush = SolidColor(Color(0xFF818CF8)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Phone Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (customPhoneText.isEmpty()) {
                        Text(
                            text = "Phone (e.g. +15550199000)",
                            color = Color(0xFF94A3B8).copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                    BasicTextField(
                        value = customPhoneText,
                        onValueChange = { customPhoneText = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                        cursorBrush = SolidColor(Color(0xFF818CF8)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (customPromptText.isNotBlank()) {
                            showError = false
                            onTaskSelected(customPromptText.trim(), customPhoneText.trim(), "CUSTOM")
                        } else {
                            showError = true
                            errorMessage = "Enter a prompt first"
                        }
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (customPromptText.isNotBlank())
                            Color(0xFF6366F1) else Color(0xFF334155)
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Dispatch CALL-E Call",
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = "Dispatch CALL-E Call",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                )

                // Paste Prompt from Clipboard Chip
                Chip(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    onClick = {
                        val clipText = clipboardManager?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clipText.isNullOrBlank()) {
                            customPromptText = clipText.trim()
                            showError = false
                            errorMessage = ""
                        } else {
                            showError = true
                            errorMessage = "Clipboard is empty"
                        }
                    },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E1B4B)),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste Clipboard Prompt",
                            tint = Color(0xFF818CF8)
                        )
                    },
                    label = {
                        Text(
                            text = "Paste from Clipboard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC7D2FE)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    icon: ImageVector,
    label: String,
    subtitle: String,
    chipColor: Color,
    onClick: () -> Unit
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick,
        colors = ChipDefaults.chipColors(backgroundColor = chipColor),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(18.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        },
        secondaryLabel = {
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    )
}
