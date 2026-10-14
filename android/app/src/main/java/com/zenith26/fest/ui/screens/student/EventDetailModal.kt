package com.zenith26.fest.ui.screens.student

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.ui.theme.Amber200
import com.zenith26.fest.ui.theme.Amber50
import com.zenith26.fest.ui.theme.Amber500
import com.zenith26.fest.ui.theme.Amber800
import com.zenith26.fest.ui.theme.Emerald200
import com.zenith26.fest.ui.theme.Emerald50
import com.zenith26.fest.ui.theme.Emerald600
import com.zenith26.fest.ui.theme.Emerald700
import com.zenith26.fest.ui.theme.Indigo200
import com.zenith26.fest.ui.theme.Indigo600
import com.zenith26.fest.ui.theme.Indigo950
import com.zenith26.fest.ui.theme.Pink500
import com.zenith26.fest.ui.theme.Rose200
import com.zenith26.fest.ui.theme.Rose50
import com.zenith26.fest.ui.theme.Rose600
import com.zenith26.fest.ui.theme.Slate100
import com.zenith26.fest.ui.theme.Slate200
import com.zenith26.fest.ui.theme.Slate300
import com.zenith26.fest.ui.theme.Slate400
import com.zenith26.fest.ui.theme.Slate50
import com.zenith26.fest.ui.theme.Slate600
import com.zenith26.fest.ui.theme.Slate700
import com.zenith26.fest.ui.theme.Slate800
import com.zenith26.fest.ui.theme.Slate900

sealed class ModalAlert {
    data class Success(val message: String) : ModalAlert()
    data class Warning(val message: String) : ModalAlert()
    data class Error(val message: String) : ModalAlert()
}

@Composable
fun EventDetailModal(
    event: Event,
    isSubmitting: Boolean,
    modalAlert: ModalAlert?,
    onDismiss: () -> Unit,
    onSubmitRegistration: (Int, RegistrationRequest) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var rollError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val isFull = event.isFull
    val filledPct = if (event.capacity > 0) {
        ((event.capacity - event.availableSeats).toFloat() / event.capacity).coerceIn(0f, 1f)
    } else 0f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Dark Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Slate900, Indigo950, Color(0xFF2E1065))
                            )
                        )
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = event.icon ?: "⚡", fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = event.category.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Indigo200,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = event.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4 Metric Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MetricChip(label = "Date", value = event.eventDate, modifier = Modifier.weight(1f))
                            MetricChip(label = "Time", value = event.eventTime, modifier = Modifier.weight(1f))
                            MetricChip(label = "Venue", value = event.venue, modifier = Modifier.weight(1f))
                            MetricChip(label = "Entry Fee", value = event.feeDisplay, isHighlight = true, modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Modal Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // About section
                    Text(
                        text = "ABOUT THIS EVENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate400,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = event.description ?: "No description provided.",
                        fontSize = 13.sp,
                        color = Slate600,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seat capacity bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Seat Capacity Status",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700
                                )
                                Text(
                                    text = if (isFull) "EVENT IS FULL (0 seats left)" else "${event.availableSeats} of ${event.capacity} seats left",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFull) Rose600 else Indigo600
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { filledPct },
                                color = if (isFull) Rose600 else if (filledPct > 0.8f) Amber500 else Indigo600,
                                trackColor = Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Alert Banner
                    if (modalAlert != null) {
                        val (bgColor, borderColor, textColor, icon) = when (modalAlert) {
                            is ModalAlert.Success -> Quad(Emerald50, Emerald200, Emerald700, "✅")
                            is ModalAlert.Warning -> Quad(Amber50, Amber200, Amber800, "⚠️")
                            is ModalAlert.Error -> Quad(Rose50, Rose200, Color(0xFF991B1B), "❌")
                        }
                        val alertMsg = when (modalAlert) {
                            is ModalAlert.Success -> modalAlert.message
                            is ModalAlert.Warning -> modalAlert.message
                            is ModalAlert.Error -> modalAlert.message
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(text = icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = alertMsg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Registration Form Section
                    Text(
                        text = "STUDENT REGISTRATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate800,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Slate200, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Full Name
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = {
                            studentName = it
                            nameError = null
                        },
                        label = { Text("Full Name *", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Aditi Sharma", fontSize = 12.sp) },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = Rose600, fontSize = 11.sp) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isFull && !isSubmitting
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Roll Number
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = {
                            rollNumber = it
                            rollError = null
                        },
                        label = { Text("Roll / Student ID *", fontSize = 12.sp) },
                        placeholder = { Text("e.g. 26CS104", fontSize = 12.sp) },
                        isError = rollError != null,
                        supportingText = rollError?.let { { Text(it, color = Rose600, fontSize = 11.sp) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isFull && !isSubmitting
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Phone Number (Optional)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (Optional)", fontSize = 12.sp) },
                        placeholder = { Text("e.g. 9876543210", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isFull && !isSubmitting
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("Email Address *", fontSize = 12.sp) },
                        placeholder = { Text("e.g. student@college.edu", fontSize = 12.sp) },
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = Rose600, fontSize = 11.sp) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isFull && !isSubmitting
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Registration Button
                    Button(
                        onClick = {
                            // Client-side validations
                            var hasErr = false
                            if (studentName.trim().isBlank()) {
                                nameError = "Please enter your full name"
                                hasErr = true
                            }
                            if (rollNumber.trim().isBlank()) {
                                rollError = "Please enter your roll / student number"
                                hasErr = true
                            }
                            val em = email.trim()
                            if (em.isBlank()) {
                                emailError = "Please enter your email address"
                                hasErr = true
                            } else if (!em.contains("@") || em.startsWith("@") || em.endsWith("@")) {
                                emailError = "Please enter a valid email address containing '@'"
                                hasErr = true
                            }

                            if (!hasErr) {
                                onSubmitRegistration(
                                    event.id,
                                    RegistrationRequest(
                                        studentName = studentName.trim(),
                                        rollNumber = rollNumber.trim(),
                                        email = email.trim(),
                                        phone = phone.trim().ifBlank { null }
                                    )
                                )
                            }
                        },
                        enabled = !isFull && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Indigo600,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = Slate400
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Registering...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        } else {
                            Text(
                                text = if (isFull) "Event is Full" else "Confirm Registration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun MetricChip(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Slate300
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) Emerald200 else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
