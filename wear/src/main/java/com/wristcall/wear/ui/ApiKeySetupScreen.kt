package com.wristcall.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberScalingLazyListState

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.ui.platform.LocalContext

/**
 * Wear OS API Key Onboarding Screen enforcing CALL-E setup before accessing main screen.
 */
@Composable
fun ApiKeySetupScreen(
    onApiKeySaved: (apiKey: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }
    val listState = rememberScalingLazyListState()
    var inputKey by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617)),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "API Key Required",
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        item {
            Text(
                text = "CALL-E Setup Required",
                style = MaterialTheme.typography.caption1,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "Enter or paste your CALL-E API Key below.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Real API Key BasicTextField Container
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(
                            if (showError) Color(0xFF7F1D1D) else Color(0xFF1E293B)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputKey.isEmpty()) {
                        Text(
                            text = "Enter Key (iams_live_...)",
                            color = if (showError) Color(0xFFFCA5A5) else Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                    BasicTextField(
                        value = inputKey,
                        onValueChange = {
                            inputKey = it
                            if (it.isNotBlank()) {
                                showError = false
                                errorMessage = ""
                            }
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        cursorBrush = SolidColor(Color(0xFF818CF8)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Validation error message
                if (showError && errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 10.sp,
                        color = Color(0xFFF87171),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (inputKey.isBlank()) {
                            showError = true
                            errorMessage = "⚠ Please enter your API key first"
                        } else {
                            showError = false
                            errorMessage = ""
                            onApiKeySaved(inputKey.trim())
                        }
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (inputKey.isNotBlank()) Color(0xFF4F46E5) else Color(0xFF334155)
                    ),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Save Key",
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = "Save API Key",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                )

                // Paste Key from Clipboard Chip
                Chip(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    onClick = {
                        val clipText = clipboardManager?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clipText.isNullOrBlank()) {
                            inputKey = clipText.trim()
                            // Don't auto-save, let user review the pasted key first
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
                            contentDescription = "Paste Clipboard Key",
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

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Quick Demo Key Chip
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApiKeySaved("DEMO_KEY") },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
                label = {
                    Text(
                        text = "Use Simulation DEMO Key",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                }
            )
        }
    }
}

