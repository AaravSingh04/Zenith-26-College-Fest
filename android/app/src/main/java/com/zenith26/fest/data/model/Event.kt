package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

/**
 * Event model matching FastAPI EventOut / EventDetail schema.
 */
data class Event(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("event_date")
    val eventDate: String,

    @SerializedName("event_time")
    val eventTime: String,

    @SerializedName("venue")
    val venue: String,

    @SerializedName("capacity")
    val capacity: Int,

    @SerializedName("fee")
    val fee: Int = 0,

    @SerializedName("icon")
    val icon: String? = "⚡",

    @SerializedName("available_seats")
    val availableSeats: Int
) {
    val isFull: Boolean
        get() = availableSeats <= 0

    val registeredCount: Int
        get() = (capacity - availableSeats).coerceAtLeast(0)

    val utilizationPct: Int
        get() = if (capacity > 0) ((registeredCount.toDouble() / capacity) * 100).toInt() else 0

    val feeDisplay: String
        get() = if (fee > 0) "₹$fee" else "Free"
}

/**
 * Request body for creating an event (Admin feature).
 */
data class EventCreateRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("event_date")
    val eventDate: String,

    @SerializedName("event_time")
    val eventTime: String,

    @SerializedName("venue")
    val venue: String,

    @SerializedName("capacity")
    val capacity: Int,

    @SerializedName("fee")
    val fee: Int = 0,

    @SerializedName("icon")
    val icon: String? = "🎉"
)
