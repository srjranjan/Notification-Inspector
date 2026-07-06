package com.srj.notificationinspector

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srj.notificationinspector.ui.NotificationInspectorApp
import kotlinx.coroutines.launch

@Composable
fun App(context: PlatformContext) {
    val repository = remember(context) { getNotificationRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var showInspector by remember { mutableStateOf(false) }

    // Mock Payload list for FCM & APNs simulation
    val mockPayloads = listOf(
        Triple(
            "Order Shipped 📦",
            "Your parcel #9910 has been dispatched via DHL.",
            """
            {
              "notificationId": 9910,
              "carrier": "DHL Express",
              "status": "In_Transit",
              "destination": "San Francisco, CA",
              "estimatedDelivery": "2026-07-10",
              "packageDetails": {
                "weightKg": 1.4,
                "items": [
                  {"id": "itm-01", "name": "Kotlin KMP Book", "quantity": 1},
                  {"id": "itm-02", "name": "USB-C Hub", "quantity": 2}
                ]
              }
            }
            """.trimIndent()
        ),
        Triple(
            "Flash Sale Alert! 🔥",
            "Get 50% discount on all premium courses tonight.",
            """
            {
              "aps": {
                "alert": {
                  "title": "Flash Sale Alert! 🔥",
                  "body": "Get 50% discount on all premium courses tonight."
                },
                "sound": "default",
                "badge": 1
              },
              "campaignId": "sale_2026_july",
              "discountCode": "KMP50",
              "validUntil": "23:59:59 PST"
            }
            """.trimIndent()
        ),
        Triple(
            "Security Login Attempt 🛡️",
            "New login from Mac Chrome, Paris, France.",
            """
            {
              "eventId": "evt-7861",
              "timestamp": 1783382400000,
              "security": {
                "action": "LOGIN_ATTEMPT",
                "success": false,
                "device": {
                  "os": "macOS",
                  "browser": "Chrome",
                  "ipAddress": "192.168.1.110"
                },
                "location": {
                  "city": "Paris",
                  "country": "France",
                  "coordinates": {"lat": 48.8566, "lon": 2.3522}
                }
              }
            }
            """.trimIndent()
        )
    )

    var mockIndex by remember { mutableIntStateOf(0) }

    if (showInspector) {
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    containerColor = Color(0xFF1E293B),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Button(
                        onClick = { showInspector = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Return to Simulation Dashboard", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NotificationInspectorApp(repository = repository)
            }
        }
    } else {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF38BDF8),
                background = Color(0xFF0F172A),
                surface = Color(0xFF1E293B)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔔\nNotification Inspector\nSimulation Deck",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Text(
                        text = "Simulate and intercept push notification payloads directly on your device, view logs in real-time, and inspect JSON with collapsible tree folding.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Interception Card Button
                    Button(
                        onClick = {
                            val mock = mockPayloads[mockIndex]
                            coroutineScope.launch {
                                repository.insertLog(
                                    title = mock.first,
                                    body = mock.second,
                                    rawPayload = mock.third
                                )
                            }
                            mockIndex = (mockIndex + 1) % mockPayloads.size
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simulate Intercept Push 🚀", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Open Inspector Interface Button
                    Button(
                        onClick = { showInspector = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open Inspector Console 🔍", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}
