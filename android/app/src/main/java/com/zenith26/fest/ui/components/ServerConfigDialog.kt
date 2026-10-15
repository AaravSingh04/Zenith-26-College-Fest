package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zenith26.fest.data.remote.ApiClient
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate600
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate900

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSaveUrl: (String) -> Unit
) {
    var urlText by remember { mutableStateOf(currentUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌐", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backend Server URL",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Configure the FastAPI server endpoint for Android communication.",
                    fontSize = 12.sp,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Text(
                    text = "Quick Presets:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PresetChip(
                        label = "🚀 Vercel Cloud Production (24/7 Global)",
                        url = ApiClient.DEFAULT_VERCEL_URL,
                        onClick = { urlText = ApiClient.DEFAULT_VERCEL_URL }
                    )
                    PresetChip(
                        label = "☁️ Cloud Tunnel (Appetize.io / Remote Phone)",
                        url = ApiClient.DEFAULT_TUNNEL_URL,
                        onClick = { urlText = ApiClient.DEFAULT_TUNNEL_URL }
                    )
                    PresetChip(
                        label = "📱 Android Emulator (10.0.2.2:8080)",
                        url = ApiClient.DEFAULT_EMULATOR_URL,
                        onClick = { urlText = ApiClient.DEFAULT_EMULATOR_URL }
                    )
                    PresetChip(
                        label = "💻 Local Dev Machine (172.16.51.33:8080)",
                        url = ApiClient.DEFAULT_LAN_URL,
                        onClick = { urlText = ApiClient.DEFAULT_LAN_URL }
                    )
                    PresetChip(
                        label = "🏠 Localhost (127.0.0.1:8080)",
                        url = ApiClient.DEFAULT_LOCALHOST_URL,
                        onClick = { urlText = ApiClient.DEFAULT_LOCALHOST_URL }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Base URL", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Slate600, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSaveUrl(urlText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Indigo600,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save & Apply", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    url: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Slate100)
            .border(1.dp, Slate200, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                text = url,
                fontSize = 10.sp,
                color = Slate400
            )
        }
    }
}
