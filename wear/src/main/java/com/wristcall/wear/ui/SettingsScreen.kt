package com.wristcall.wear.ui

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import com.calle.sdk.data.CallEPreferences

/**
 * Settings Screen for viewing and editing CALL-E, SerpApi, and Groq AI API Keys.
 */
@Composable
fun SettingsScreen(
    onSaveCompleted: (callEKey: String, serpKey: String, groqKey: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { CallEPreferences(context) }
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }
    val listState = rememberScalingLazyListState()

    var callEKeyInput by remember { mutableStateOf(prefs.apiKey) }
    var serpKeyInput by remember { mutableStateOf(prefs.serpApiKey) }
    var groqKeyInput by remember { mutableStateOf(prefs.groqApiKey) }

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon Header
        item {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "API Settings",
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        item {
            Text(
                text = "⚙️ API Settings",
                style = MaterialTheme.typography.caption1,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Section 1: CALL-E Key
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🔑 CALL-E API Key:",
                    fontSize = 10.sp,
                    color = Color(0xFF818CF8),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    if (callEKeyInput.isEmpty()) {
                        Text("Enter CALL-E Key...", color = Color(0xFF64748B), fontSize = 10.sp)
                    }
                    BasicTextField(
                        value = callEKeyInput,
                        onValueChange = { callEKeyInput = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                        cursorBrush = SolidColor(Color(0xFF818CF8)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 2: SerpApi Key
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🔍 SerpApi Google Key:",
                    fontSize = 10.sp,
                    color = Color(0xFF34D399),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Color(0xFF064E3B))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    if (serpKeyInput.isEmpty()) {
                        Text("Enter SerpApi Key...", color = Color(0xFF047857), fontSize = 10.sp)
                    }
                    BasicTextField(
                        value = serpKeyInput,
                        onValueChange = { serpKeyInput = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                        cursorBrush = SolidColor(Color(0xFF34D399)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 3: Groq AI Key
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🧠 Groq AI Key (120B):",
                    fontSize = 10.sp,
                    color = Color(0xFFF472B6),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Color(0xFF831843))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    if (groqKeyInput.isEmpty()) {
                        Text("Enter Groq Key...", color = Color(0xFF9D174D), fontSize = 10.sp)
                    }
                    BasicTextField(
                        value = groqKeyInput,
                        onValueChange = { groqKeyInput = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                        cursorBrush = SolidColor(Color(0xFFF472B6)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save All Button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    prefs.apiKey = callEKeyInput.trim()
                    prefs.serpApiKey = serpKeyInput.trim()
                    prefs.groqApiKey = groqKeyInput.trim()
                    onSaveCompleted(callEKeyInput.trim(), serpKeyInput.trim(), groqKeyInput.trim())
                },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF4338CA)),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save All Settings",
                        tint = Color.White
                    )
                },
                label = {
                    Text(
                        text = "Save All Settings",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        }

        // Back / Cancel Button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack,
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
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
