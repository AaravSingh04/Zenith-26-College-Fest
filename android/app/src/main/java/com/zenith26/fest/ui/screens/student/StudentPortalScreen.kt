package com.zenith26.fest.ui.screens.student

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.remote.NetworkResult
import com.zenith26.fest.ui.components.CategoryFilterChips
import com.zenith26.fest.ui.components.EmptyView
import com.zenith26.fest.ui.components.ErrorView
import com.zenith26.fest.ui.components.EventCard
import com.zenith26.fest.ui.components.HeroSection
import com.zenith26.fest.ui.components.LoadingView
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate50
import com.zenith26.fest.ui.theme.Slate500
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.viewmodel.StudentViewModel

@Composable
fun StudentPortalScreen(
    viewModel: StudentViewModel
) {
    val eventsState by viewModel.eventsState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val modalAlert by viewModel.modalAlert.collectAsState()

    val totalEvents = if (eventsState is NetworkResult.Success) {
        (eventsState as NetworkResult.Success).data.size
    } else 14

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Hero Section
        item {
            HeroSection(totalEventsCount = totalEvents)
        }

        // Search & Category Filter Controls Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .border(1.dp, Slate200, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = "Search events by title (e.g. Code, Bands)...",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Indigo600,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Pills
                    CategoryFilterChips(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setCategory(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Showing count & Reset button
                    val hasFilter = selectedCategory != "All" || searchQuery.isNotEmpty()
                    val count = if (eventsState is NetworkResult.Success) {
                        (eventsState as NetworkResult.Success).data.size
                    } else 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (eventsState is NetworkResult.Success) {
                                "Showing $count ${if (count == 1) "event" else "events"}${
                                    if (selectedCategory != "All") " in $selectedCategory" else ""
                                }${if (searchQuery.isNotEmpty()) " matching \"$searchQuery\"" else ""}"
                            } else {
                                "Loading events..."
                            },
                            fontSize = 11.sp,
                            color = Slate500
                        )

                        if (hasFilter) {
                            Text(
                                text = "Reset filters",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo600,
                                modifier = Modifier.clickable { viewModel.clearFilters() }
                            )
                        }
                    }
                }
            }
        }

        // State Content: Loading, Error, Empty, or Event Cards
        when (val state = eventsState) {
            is NetworkResult.Loading -> {
                item {
                    LoadingView()
                }
            }
            is NetworkResult.Error -> {
                item {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadEvents() }
                    )
                }
            }
            is NetworkResult.Success -> {
                val events = state.data
                if (events.isEmpty()) {
                    item {
                        EmptyView(onClearFilters = { viewModel.clearFilters() })
                    }
                } else {
                    items(events, key = { it.id }) { event ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            EventCard(
                                event = event,
                                onViewDetailsClick = { viewModel.openEventDetails(it) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Modal
    selectedEvent?.let { event ->
        EventDetailModal(
            event = event,
            isSubmitting = isSubmitting,
            modalAlert = modalAlert,
            onDismiss = { viewModel.dismissModal() },
            onSubmitRegistration = { id, req -> viewModel.submitRegistration(id, req) }
        )
    }
}
