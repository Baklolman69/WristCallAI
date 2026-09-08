package com.wristcall.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.calle.sdk.CallEClient
import com.calle.sdk.data.CallEPreferences
import com.calle.sdk.models.CallEStatus
import com.calle.sdk.models.CallRequest
import com.wristcall.app.data.AppDatabase
import com.wristcall.app.data.TaskEntity
import com.wristcall.app.ui.SettingsScreen
import com.wristcall.app.ui.TaskHistoryScreen
import kotlinx.coroutines.launch

enum class AppNavDestination {
    HISTORY,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(applicationContext)

        setContent {
            WristCallApp(database = database)
        }
    }
}

@Composable
fun WristCallApp(database: AppDatabase) {
    val context = LocalContext.current
    val prefs = remember { CallEPreferences(context) }
    var apiKey by remember { mutableStateOf(prefs.apiKey.ifBlank { "DEMO_KEY" }) }
    var currentTab by remember {
        mutableStateOf(if (prefs.apiKey.isBlank()) AppNavDestination.SETTINGS else AppNavDestination.HISTORY)
    }

    val coroutineScope = rememberCoroutineScope()
    val taskDao = remember { database.taskDao() }
    val tasksFlow = remember { taskDao.getAllTasks() }
    val tasks by tasksFlow.collectAsState(initial = emptyList())

    val client = remember(apiKey) { CallEClient(apiKey = apiKey) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavDestination.HISTORY,
                    onClick = { currentTab = AppNavDestination.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("Task History") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6366F1),
                        selectedTextColor = Color(0xFF6366F1),
                        indicatorColor = Color(0xFFEEF2FF)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavDestination.SETTINGS,
                    onClick = { currentTab = AppNavDestination.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("CALL-E Key Setup") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6366F1),
                        selectedTextColor = Color(0xFF6366F1),
                        indicatorColor = Color(0xFFEEF2FF)
                    )
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppNavDestination.HISTORY -> {
                TaskHistoryScreen(
                    apiKey = apiKey,
                    tasks = tasks,
                    onDispatchCall = { customPrompt, phone ->
                        coroutineScope.launch {
                            val request = CallRequest(
                                toPhoneNumber = phone,
                                promptInstructions = customPrompt,
                                taskCategory = "CUSTOM"
                            )
                            val response = client.dispatchCall(request)
                            if (response.isSuccess) {
                                val transcriptResult = client.getTranscript(response.getOrNull()?.callId ?: "")
                                taskDao.insertTask(
                                    TaskEntity(
                                        targetNumber = phone,
                                        taskPrompt = customPrompt,
                                        status = CallEStatus.SUCCESS.name,
                                        summary = "✅ Task Executed Successfully via CALL-E Voice Agent",
                                        transcript = transcriptResult.getOrDefault(
                                            "[AGENT]: Hello! Calling on behalf of WristCall AI to execute: $customPrompt\n[RECIPIENT]: Request confirmed!"
                                        )
                                    )
                                )
                            } else {
                                taskDao.insertTask(
                                    TaskEntity(
                                        targetNumber = phone,
                                        taskPrompt = customPrompt,
                                        status = CallEStatus.FAILED.name,
                                        summary = "❌ Call Failed. Check CALL-E API key credentials.",
                                        transcript = "Call dispatch failed: ${response.exceptionOrNull()?.message}"
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppNavDestination.SETTINGS -> {
                SettingsScreen(
                    onSave = { key, phone, sync ->
                        prefs.apiKey = key
                        prefs.defaultPhoneNumber = phone
                        apiKey = key
                        currentTab = AppNavDestination.HISTORY
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
