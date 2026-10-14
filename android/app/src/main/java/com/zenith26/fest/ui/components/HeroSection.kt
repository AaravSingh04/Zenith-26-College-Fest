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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenith26.fest.ui.theme.Cyan300
import com.zenith26.fest.ui.theme.Indigo300
import com.zenith26.fest.ui.theme.Indigo900
import com.zenith26.fest.ui.theme.Indigo950
import com.zenith26.fest.ui.theme.Pink300
import com.zenith26.fest.ui.theme.Pink400
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate900

@Composable
fun HeroSection(
    totalEventsCount: Int = 14,
    totalSeatsCount: Int = 1500
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Indigo900,
                        Indigo950,
                        Slate900
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Schedule & Venue Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Nov 14 – 15, 2026",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo300
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Main Campus",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate300
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Headline
            Text(
                text = "Ignite Your Passion.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            Text(
                text = "Unleash The Future.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Pink400,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Explore 14 flagship competitions, concerts, esports face-offs, and skill workshops. Grab your seat before registrations close!",
                fontSize = 12.sp,
                color = Slate300,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Metrics Counter Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalEventsCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Indigo300
                    )
                    Text(
                        text = "Events",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (totalSeatsCount > 0) "$totalSeatsCount" else "1,500+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Pink300
                    )
                    Text(
                        text = "Total Seats",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "2 Days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Cyan300
                    )
                    Text(
                        text = "Action",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400
                    )
                }
            }
        }
    }
}
