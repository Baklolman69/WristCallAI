package com.wristcall.wear.ui

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Search
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

/**
 * Optional SerpApi Setup Screen highlighting Google Search Business Phone Discovery benefits.
 */
@Composable
fun SerpApiSetupScreen(
    currentSerpKey: String,
    onSerpKeySaved: (serpKey: String) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }
    val listState = rememberScalingLazyListState()
    var inputKey by remember {
        mutableStateOf(currentSerpKey)
    }

    ScalingLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF022C22)),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon Header
        item {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "SerpApi Google Search",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        item {
            Text(
                text = "Google SerpApi Setup",
                style = MaterialTheme.typography.caption1,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        item {
            Text(
                text = "Optional • Enable Smart Business Discovery",
                fontSize = 10.sp,
                color = Color(0xFF6EE7B7),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // Benefits Highlight Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF064E3B))
                    .padding(10.dp)
            ) {
                Text(
                    text = "🌟 Benefits of SerpApi:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA7F3D0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                BenefitItem("🔍 Auto Phone Lookup", "Finds restaurant & shop numbers online")
                BenefitItem("📍 No Typing Needed", "Say place name, AI finds the phone")
                BenefitItem("🤖 Groq AI Refinement", "Pulls menus, hours & location info")
            }
        }

        // API Key Input Field
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SerpApi Key:",
                    fontSize = 10.sp,
                    color = Color(0xFFA7F3D0),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Color(0xFF022C22))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputKey.isEmpty()) {
                        Text(
                            text = "Enter SerpApi Key...",
                            color = Color(0xFF047857),
                            fontSize = 10.sp
                        )
                    }
                    BasicTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                        cursorBrush = SolidColor(Color(0xFF34D399)),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Paste from Clipboard Chip
        item {
            Chip(
                modifier = Modifier.fillMaxWidth().height(32.dp),
                onClick = {
                    val clipText = clipboardManager?.primaryClip?.getItemAt(0)?.text?.toString()
                    if (!clipText.isNullOrBlank()) {
                        inputKey = clipText.trim()
                    }
                },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF064E3B)),
                icon = {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste Clipboard",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(14.dp)
                    )
                },
                label = {
                    Text(
                        text = "Paste from Clipboard",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA7F3D0)
                    )
                }
            )
        }

        // Save & Continue Button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onSerpKeySaved(inputKey.trim())
                },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF059669)),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save & Continue",
                        tint = Color.White
                    )
                },
                label = {
                    Text(
                        text = "Save & Continue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        }

        // Skip Button
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSkip,
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
                icon = {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Skip for Now",
                        tint = Color(0xFF94A3B8)
                    )
                },
                label = {
                    Text(
                        text = "Skip for Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            )
        }
    }
}

@Composable
private fun BenefitItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = desc, fontSize = 8.sp, color = Color(0xFF6EE7B7))
    }
}
