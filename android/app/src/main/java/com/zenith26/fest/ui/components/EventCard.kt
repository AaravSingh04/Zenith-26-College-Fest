package com.zenith26.fest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.ui.theme.Amber600
import com.zenith26.fest.ui.theme.Emerald600
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Rose600
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate50
import com.zenith26.fest.ui.theme.Slate500
import com.zenith26.fest.ui.theme.Slate600
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.getCategoryStyle

@Composable
fun EventCard(
    event: Event,
    onViewDetailsClick: (Event) -> Unit
) {
    val isFull = event.isFull
    val catStyle = getCategoryStyle(event.category)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isFull) Slate300 else Slate200,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // Top Section (Padding 16dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header: Category Pill + Capacity / FULL Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(catStyle.bg)
                            .border(1.dp, catStyle.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = event.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = catStyle.text
                        )
                    }

                    // Seat Status
                    if (isFull) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Rose600)
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FULL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    } else {
                        val seatColor = if (event.availableSeats <= 5) Amber600 else Emerald600
                        Text(
                            text = "${event.availableSeats} / ${event.capacity} seats left",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = seatColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title & Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate100)
                            .border(1.dp, Slate200, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = event.icon ?: "⚡",
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = event.description ?: "Exciting fest event. Register now!",
                            fontSize = 12.sp,
                            color = Slate500,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalDivider(color = Slate100, thickness = 1.dp)

                Spacer(modifier = Modifier.height(10.dp))

                // Event Meta: Date, Time & Fee
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📅", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.eventDate,
                            fontSize = 12.sp,
                            color = Slate600,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "•", fontSize = 12.sp, color = Slate400)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.eventTime,
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }

                    Text(
                        text = event.feeDisplay,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (event.fee > 0) Indigo600 else Emerald600
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Venue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📍", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.venue,
                        fontSize = 12.sp,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Footer (Slate-50 background)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(width = 1.dp, color = Slate100)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFull) "Registration Closed" else "Open for students",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isFull) Rose600 else Slate500
                )

                Button(
                    onClick = { onViewDetailsClick(event) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFull) Color(0xFFE2E8F0) else Indigo600,
                        contentColor = if (isFull) Slate700 else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 6.dp
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "View Details →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        }
    }
}
