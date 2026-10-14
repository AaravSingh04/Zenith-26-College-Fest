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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.ui.theme.Indigo50
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Indigo700
import com.zenith26.fest.ui.theme.Pink500
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate50
import com.zenith26.fest.ui.theme.Slate600
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.Violet600

enum class AppScreen {
    STUDENT_PORTAL,
    ADMIN_DASHBOARD
}

@Composable
fun ZenithTopBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    onOpenServerConfig: () -> Unit
) {
    Surface(
        color = Slate50,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Slate200)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Row 1: Logo, Title, Badge, and Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onScreenSelected(AppScreen.STUDENT_PORTAL) }
                ) {
                    // Logo Icon with Gradient
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Violet600, Indigo600, Pink500)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ZENITH '26",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Indigo700
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Indigo50)
                                    .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(50))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (currentScreen == AppScreen.ADMIN_DASHBOARD) "ADMIN" else "FEST",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Indigo700
                                )
                            }
                        }
                        Text(
                            text = "Campus Event Management System",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600
                        )
                    }
                }

                // Server Config Icon Button
                IconButton(
                    onClick = onOpenServerConfig,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Server Settings",
                        tint = Slate600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Row 2: Nav Tabs (Student Portal / Admin Dashboard)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                // Student Portal Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (currentScreen == AppScreen.STUDENT_PORTAL) Indigo50 else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (currentScreen == AppScreen.STUDENT_PORTAL) Color(0xFFC7D2FE) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onScreenSelected(AppScreen.STUDENT_PORTAL) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Student Portal",
                        fontSize = 13.sp,
                        fontWeight = if (currentScreen == AppScreen.STUDENT_PORTAL) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (currentScreen == AppScreen.STUDENT_PORTAL) Indigo600 else Slate600
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Admin Dashboard Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (currentScreen == AppScreen.ADMIN_DASHBOARD) Slate900 else Color(0xFFF1F5F9)
                        )
                        .border(
                            width = 1.dp,
                            color = if (currentScreen == AppScreen.ADMIN_DASHBOARD) Slate900 else Slate200,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onScreenSelected(AppScreen.ADMIN_DASHBOARD) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📊", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Admin Dashboard",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentScreen == AppScreen.ADMIN_DASHBOARD) Slate50 else Slate600
                        )
                    }
                }
            }
        }
    }
}
