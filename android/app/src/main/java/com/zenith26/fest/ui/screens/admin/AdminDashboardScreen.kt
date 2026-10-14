package com.zenith26.fest.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.data.model.StatsResponse
import com.zenith26.fest.ui.components.ErrorView
import com.zenith26.fest.ui.components.LoadingView
import com.zenith26.fest.ui.theme.Amber200
import com.zenith26.fest.ui.theme.Amber50
import com.zenith26.fest.ui.theme.Amber500
import com.zenith26.fest.ui.theme.Emerald200
import com.zenith26.fest.ui.theme.Emerald50
import com.zenith26.fest.ui.theme.Emerald600
import com.zenith26.fest.ui.theme.Indigo50
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Purple600
import com.zenith26.fest.ui.theme.Rose50
import com.zenith26.fest.ui.theme.Rose600
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate50
import com.zenith26.fest.ui.theme.Slate500
import com.zenith26.fest.ui.theme.Slate600
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.getCategoryStyle
import com.zenith26.fest.ui.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val events by viewModel.events.collectAsState()
    val registrations by viewModel.registrations.collectAsState()
    val selectedRegFilterEventId by viewModel.selectedRegFilterEventId.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val showAddEventDialog by viewModel.showAddEventDialog.collectAsState()
    val pendingDeleteReg by viewModel.pendingDeleteReg.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Top Action Header (Refresh + Add Event)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ADMIN CONSOLE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFA5B4FC)
                            )
                        }
                        Text(
                            text = "Live SQLite & FastAPI Analytics",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Refresh Data button
                        OutlinedButton(
                            onClick = { viewModel.loadAllData() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Slate200
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("🔄 Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Add Event button
                        Button(
                            onClick = { viewModel.openAddEventDialog() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Indigo600,
                                contentColor = Color.White
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            ),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("➕ Add Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            item {
                LoadingView(message = "Computing live dashboard analytics...")
            }
        } else if (errorMessage != null && stats == null) {
            item {
                ErrorView(
                    message = errorMessage!!,
                    onRetry = { viewModel.loadAllData() }
                )
            }
        } else {
            // 4 Core Stat Cards
            item {
                stats?.let { s ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "TOTAL EVENTS",
                                value = "${s.totalEvents}",
                                subtitle = "scheduled",
                                note = "Across fest categories",
                                icon = "📅",
                                accentColor = Indigo600,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "TOTAL REGISTRATIONS",
                                value = "${s.totalRegistrations}",
                                subtitle = "students",
                                note = "Live verified count",
                                icon = "🎟️",
                                accentColor = Emerald600,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "CAPACITY UTILIZATION",
                                value = "${s.overallUtilizationPct}%",
                                subtitle = "${s.totalRegistrations} / ${s.totalCapacity}",
                                note = "Overall student ratio",
                                icon = "📈",
                                accentColor = Purple600,
                                progressPct = (s.overallUtilizationPct / 100.0).toFloat().coerceIn(0f, 1f),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "UPCOMING EVENTS",
                                value = "${s.upcomingEventsCount}",
                                subtitle = "events",
                                note = "Nov 14-15 fest schedule",
                                icon = "⏳",
                                accentColor = Amber500,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Category Breakdown Section
            item {
                stats?.let { s ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "EVENTS BY CATEGORY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate700,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Live Breakdown",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val catMap = s.eventsByCategory
                                val topCats = listOf("Technical", "Cultural", "Sports", "Gaming", "Workshop")
                                topCats.forEach { catName ->
                                    val count = catMap[catName] ?: 0
                                    val style = getCategoryStyle(catName)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(style.bg)
                                            .border(1.dp, style.border, RoundedCornerShape(10.dp))
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = catName,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = style.text,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$count",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Slate900
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab Navigation (Events / Registrations)
            item {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, top = 14.dp)
                        .border(1.dp, Slate200, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Tab 1: Events
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentTab == 0) Indigo50 else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (currentTab == 0) Color(0xFFC7D2FE) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setTab(0) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📋 Events (${events.size})",
                                fontSize = 12.sp,
                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (currentTab == 0) Indigo600 else Slate600
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Tab 2: Registrations
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentTab == 1) Indigo50 else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (currentTab == 1) Color(0xFFC7D2FE) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setTab(1) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👥 Registrations (${registrations.size})",
                                fontSize = 12.sp,
                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (currentTab == 1) Indigo600 else Slate600
                            )
                        }
                    }
                }
            }

            // Tab 1 Content: Events List (sorted by utilization descending)
            if (currentTab == 0) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Sorted by Utilization % Descending (Critical >90% Highlighted)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate400
                        )
                    }
                }

                items(events, key = { it.id }) { event ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF1F5F9))
                    ) {
                        AdminEventItem(event = event)
                    }
                }
            }

            // Tab 2 Content: Registrations List
            if (currentTab == 1) {
                // Dropdown Filter by Event
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .background(Color.White)
                            .padding(14.dp)
                    ) {
                        EventFilterSelector(
                            events = events,
                            selectedEventId = selectedRegFilterEventId,
                            onEventSelected = { viewModel.filterRegistrationsByEvent(it) },
                            totalRegCount = registrations.size
                        )
                    }
                }

                if (registrations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .background(Color.White)
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "📝", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Registrations Yet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700
                                )
                                Text(
                                    text = "Students have not registered for this event yet.",
                                    fontSize = 11.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }
                } else {
                    items(registrations, key = { it.id }) { reg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF1F5F9))
                        ) {
                            AdminRegistrationItem(
                                reg = reg,
                                onCancelClick = { viewModel.promptDeleteRegistration(it) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { viewModel.dismissAddEventDialog() },
            onSave = { viewModel.createEvent(it) }
        )
    }

    // Cancel Registration Confirmation Dialog
    pendingDeleteReg?.let { reg ->
        DeleteRegConfirmDialog(
            reg = reg,
            onDismiss = { viewModel.dismissDeleteDialog() },
            onConfirm = { viewModel.confirmDeleteRegistration() }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    note: String,
    icon: String,
    accentColor: Color,
    progressPct: Float? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.border(1.dp, Slate200, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate400,
                        letterSpacing = 0.5.sp
                    )
                    Text(text = icon, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (accentColor == Emerald600) Emerald600 else Slate900
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (progressPct != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressPct },
                        color = accentColor,
                        trackColor = Slate100,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = note, fontSize = 9.sp, color = Slate400)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accentColor)
            )
        }
    }
}

@Composable
private fun AdminEventItem(event: Event) {
    val registered = event.capacity - event.availableSeats
    val utilPct = event.utilizationPct
    val isCritical = utilPct >= 90
    val isFull = event.isFull
    val catStyle = getCategoryStyle(event.category)

    val itemBg = if (isCritical) Color(0xFFFFFBEB) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(itemBg)
            .padding(14.dp)
    ) {
        // Row 1: Title, Icon, Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = event.icon ?: "⚡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status Badge
            if (isFull) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Rose600)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FULL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else if (isCritical) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Amber500)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ">90% ALMOST FULL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Emerald50)
                        .border(1.dp, Emerald200, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Available",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Category, Date, Venue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(catStyle.bg)
                    .border(1.dp, catStyle.border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = event.category,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = catStyle.text
                )
            }

            Text(
                text = "${event.eventDate} • ${event.venue}",
                fontSize = 11.sp,
                color = Slate500
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: Utilization Progress Bar and Counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$registered / ${event.capacity} seats",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700
            )

            Spacer(modifier = Modifier.width(10.dp))

            LinearProgressIndicator(
                progress = { (utilPct / 100f).coerceIn(0f, 1f) },
                color = if (isCritical) Rose600 else Indigo600,
                trackColor = Color(0xFFE2E8F0),
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "$utilPct%",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCritical) Rose600 else Indigo600
            )
        }
    }
}

@Composable
private fun AdminRegistrationItem(
    reg: RegistrationDetail,
    onCancelClick: (RegistrationDetail) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reg.studentName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Roll: ${reg.rollNumber} • ${reg.email}",
                    fontSize = 11.sp,
                    color = Slate500
                )
                if (!reg.phone.isNullOrBlank()) {
                    Text(
                        text = "Phone: ${reg.phone}",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            // Cancel Button
            Button(
                onClick = { onCancelClick(reg) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Rose50,
                    contentColor = Rose600
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 10.dp,
                    vertical = 4.dp
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Event: ${reg.eventTitle}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Indigo600
            )
            Text(
                text = reg.registeredAt,
                fontSize = 10.sp,
                color = Slate400
            )
        }
    }
}

@Composable
private fun EventFilterSelector(
    events: List<Event>,
    selectedEventId: Int?,
    onEventSelected: (Int?) -> Unit,
    totalRegCount: Int
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelectedTitle = events.find { it.id == selectedEventId }?.let { "${it.icon ?: ""} ${it.title}" }
        ?: "All Events ($totalRegCount)"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Filter by Event:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Slate700
        )

        Box {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$currentSelectedTitle ▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate800
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Events ($totalRegCount)") },
                    onClick = {
                        onEventSelected(null)
                        expanded = false
                    }
                )
                events.forEach { ev ->
                    DropdownMenuItem(
                        text = { Text("${ev.icon ?: ""} ${ev.title}") },
                        onClick = {
                            onEventSelected(ev.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
