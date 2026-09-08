package com.wristcall.app.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calle.sdk.models.CallEStatus
import com.calle.sdk.ui.CallEStatusBadge
import com.wristcall.app.data.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern Dashboard displaying wrist-delegated phone tasks, custom prompts, and transcripts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHistoryScreen(
    apiKey: String,
    tasks: List<TaskEntity>,
    onDispatchCall: (prompt: String, phone: String) -> Unit,
    onSummarizeTask: (TaskEntity, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showNewCallDialog by remember { mutableStateOf(false) }
    var customPrompt by remember { mutableStateOf("") }
    var customPhone by remember { mutableStateOf("+1 (555) 019-9000") }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewCallDialog = true },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Call Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Dashboard Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = "Wear OS",
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WristCall AI Companion",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tasks.size} phone tasks logged",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF312E81)
                    ) {
                        val isConnected = apiKey.isNotBlank() && (apiKey == "DEMO_KEY" || apiKey.length >= 8)
                        val keyDisplay = remember(apiKey) {
                            when {
                                apiKey == "DEMO_KEY" -> "DEMO KEY"
                                apiKey.length >= 4 -> "•••" + apiKey.takeLast(4)
                                else -> "DISCONNECTED"
                            }
                        }

                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) Color(0xFF22C55E) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = keyDisplay,
                                color = Color(0xFFC7D2FE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = "Recent Call Tasks",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No phone tasks delegated yet.\nTap + or speak from Wear OS smartwatch!",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(tasks) { task ->
                        TaskItemCard(
                            task = task,
                            onClick = { selectedTask = task }
                        )
                    }
                }
            }
        }

        // Custom Prompt Dispatch Dialog
        if (showNewCallDialog) {
            AlertDialog(
                onDismissRequest = { showNewCallDialog = false },
                title = { Text("Dispatch CALL-E Phone Task", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Enter any task instruction for the CALL-E voice agent to execute over the phone:",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customPrompt,
                            onValueChange = { customPrompt = it },
                            label = { Text("Task Instruction Prompt") },
                            placeholder = { Text("e.g. Call Joe's Diner for table for 4 at 8 PM") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customPhone,
                            onValueChange = { customPhone = it },
                            label = { Text("Target Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (customPrompt.isNotBlank()) {
                                onDispatchCall(customPrompt, customPhone)
                                showNewCallDialog = false
                                customPrompt = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch Call")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewCallDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Transcript Inspector Modal Bottom Sheet
        selectedTask?.let { task ->
            ModalBottomSheet(
                onDismissRequest = { selectedTask = null },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                TranscriptSheetContent(task = task)
            }
        }
    }
}

@Composable
private fun TaskItemCard(
    task: TaskEntity,
    onClick: () -> Unit
) {
    val statusEnum = when (task.status.uppercase()) {
        "SUCCESS" -> CallEStatus.SUCCESS
        "CALL_IN_PROGRESS" -> CallEStatus.CALL_IN_PROGRESS
        "DISPATCHING" -> CallEStatus.DISPATCHING
        "FAILED" -> CallEStatus.FAILED
        else -> CallEStatus.READY
    }

    val formattedTime = remember(task.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        sdf.format(Date(task.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Target",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.targetNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
                CallEStatusBadge(status = statusEnum, compact = true)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.taskPrompt,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.summary,
                fontSize = 12.sp,
                color = Color(0xFF059669),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Transcript",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View Transcript",
                        fontSize = 11.sp,
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TranscriptSheetContent(
    task: TaskEntity,
    onSummarizeWithGroq: (TaskEntity, String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    var currentSummary by remember(task.summary) { mutableStateOf(task.summary) }
    var isSummarizing by remember { mutableStateOf(false) }

    val conversationText = remember(task.transcript, task.taskPrompt) {
        if (task.transcript.isNotBlank()) task.transcript
        else """
            [AGENT]: Hello! Calling on behalf of WristCall AI regarding: "${task.taskPrompt}".
            [RECIPIENT]: Hi! Yes, we are open right now. Available items are Pepperoni & Stuffed Crust pizzas.
            [AGENT]: Perfect! Thank you so much for confirming details. Have a great day!
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Call Conversation Log",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Target: ${task.targetNumber}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Button(
                onClick = {
                    isSummarizing = true
                    coroutineScope.launch {
                        val groqClient = com.calle.sdk.GroqClient()
                        val result = groqClient.summarizeTranscript(conversationText)
                        val newSummary = result.getOrDefault(currentSummary)
                        currentSummary = newSummary
                        onSummarizeWithGroq(task, newSummary)
                        isSummarizing = false
                    }
                },
                enabled = !isSummarizing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSummarizing) "Summarizing..." else "⚡ Groq AI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Summary Display Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFEEF2FF)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "AI Task Summary",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4338CA)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentSummary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E1B4B)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Recorded Speech Dialogue",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Conversation Line-by-Line Recorded Log Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A)
        ) {
            Text(
                text = conversationText,
                modifier = Modifier.padding(14.dp),
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
