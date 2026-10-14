package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request payload for POST /api/events/{event_id}/register.
 */
data class RegistrationRequest(
    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("roll_number")
    val rollNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String? = null
)

/**
 * Response for POST /api/events/{event_id}/register.
 */
data class RegistrationResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("registered_at")
    val registeredAt: String
)

/**
 * Item in GET /api/registrations (RegistrationDetailOut).
 */
data class RegistrationDetail(
    @SerializedName("id")
    val id: Int,

    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("event_title")
    val eventTitle: String,

    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("roll_number")
    val rollNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("registered_at")
    val registeredAt: String
)

/**
 * Generic response message (e.g. DELETE registration or error detail).
 */
data class ApiMessage(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("detail")
    val detail: String? = null
)
