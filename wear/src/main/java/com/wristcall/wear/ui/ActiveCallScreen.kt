package com.wristcall.wear.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.calle.sdk.models.CallEStatus

/**
 * Wear OS Active Call Screen displaying real-time CALL-E status, live duration timer,
 * and pulsing audio orb visualizer.
 */
@Composable
fun ActiveCallScreen(
    targetName: String = "Joe's Diner",
    status: CallEStatus = CallEStatus.CALL_IN_PROGRESS,
    liveStatusText: String = "1/4 Dispatching AI Call...",
    durationSeconds: Int = 0,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulseTransition")

    val scaleRing1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseRing1"
    )

    val scaleRing2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseRing2"
    )

    val alphaRing by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaRing"
    )

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timerStr = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing Visualizer Orb
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Pulsing Aura 2
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(scaleRing2)
                    .alpha(alphaRing * 0.5f)
                    .clip(CircleShape)
                    .background(Color(0xFF38BDF8))
            )

            // Outer Pulsing Aura 1
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(scaleRing1)
                    .alpha(alphaRing)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1))
            )

            // Center Core Orb
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF818CF8), Color(0xFF312E81))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = "Call in progress",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Target Name
        Text(
            text = targetName.take(22),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Live Timer
        Text(
            text = timerStr,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF38BDF8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Live CALL-E Status Phase text
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = liveStatusText,
                fontSize = 9.sp,
                color = Color(0xFFC7D2FE),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
