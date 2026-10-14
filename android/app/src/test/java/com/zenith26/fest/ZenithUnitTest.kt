package com.zenith26.fest

import com.google.gson.Gson
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.model.RegistrationResponse
import com.zenith26.fest.data.model.StatsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Zenith '26 Android Application logic and models.
 */
class ZenithUnitTest {

    private val gson = Gson()

    @Test
    fun testEventModelCalculations() {
        val event = Event(
            id = 1,
            title = "Code Sprint 2.0",
            category = "Technical",
            description = "24-hour hackathon",
            eventDate = "2026-11-14",
            eventTime = "10:00 AM",
            venue = "Lab 3",
            capacity = 100,
            fee = 0,
            icon = "💻",
            availableSeats = 20
        )

        assertFalse(event.isFull)
        assertEquals(80, event.registeredCount)
        assertEquals(80, event.utilizationPct)
        assertEquals("Free", event.feeDisplay)
    }

    @Test
    fun testEventFullBadgeCondition() {
        val fullEvent = Event(
            id = 2,
            title = "Battle of Bands",
            category = "Cultural",
            description = "Live concert",
            eventDate = "2026-11-14",
            eventTime = "06:00 PM",
            venue = "Main Auditorium",
            capacity = 500,
            fee = 150,
            icon = "🎸",
            availableSeats = 0
        )

        assertTrue(fullEvent.isFull)
        assertEquals(500, fullEvent.registeredCount)
        assertEquals(100, fullEvent.utilizationPct)
        assertEquals("₹150", fullEvent.feeDisplay)
    }

    @Test
    fun testEmailValidationLogic() {
        fun isValidEmail(email: String): Boolean {
            val trimmed = email.trim()
            return trimmed.contains("@") && !trimmed.startsWith("@") && !trimmed.endsWith("@")
        }

        assertTrue(isValidEmail("student@college.edu"))
        assertTrue(isValidEmail("aditi.sharma@gmail.com"))
        assertFalse(isValidEmail("invalid-email"))
        assertFalse(isValidEmail("@college.edu"))
        assertFalse(isValidEmail("student@"))
        assertFalse(isValidEmail(""))
    }

    @Test
    fun testEventJsonDeserialization() {
        val json = """
            {
                "id": 1,
                "title": "Code Sprint 2.0",
                "category": "Technical",
                "description": "24-hour hackathon",
                "event_date": "2026-11-14",
                "event_time": "10:00 AM",
                "venue": "Lab 3",
                "capacity": 100,
                "fee": 0,
                "icon": "💻",
                "available_seats": 95
            }
        """.trimIndent()

        val event = gson.fromJson(json, Event::class.java)
        assertNotNull(event)
        assertEquals(1, event.id)
        assertEquals("Code Sprint 2.0", event.title)
        assertEquals(95, event.availableSeats)
        assertEquals(5, event.registeredCount)
    }

    @Test
    fun testRegistrationRequestJsonSerialization() {
        val req = RegistrationRequest(
            studentName = "Aditi Sharma",
            rollNumber = "26CS104",
            email = "aditi.sharma@college.edu",
            phone = "9876543210"
        )

        val json = gson.toJson(req)
        assertTrue(json.contains("\"student_name\":\"Aditi Sharma\""))
        assertTrue(json.contains("\"roll_number\":\"26CS104\""))
        assertTrue(json.contains("\"email\":\"aditi.sharma@college.edu\""))
    }

    @Test
    fun testStatsModelDeserialization() {
        val json = """
            {
                "total_events": 14,
                "total_registrations": 42,
                "total_capacity": 1500,
                "overall_utilization_pct": 2.8,
                "upcoming_events_count": 14,
                "events_by_category": {
                    "Technical": 4,
                    "Cultural": 5,
                    "Sports": 2,
                    "Gaming": 1,
                    "Workshop": 2
                }
            }
        """.trimIndent()

        val stats = gson.fromJson(json, StatsResponse::class.java)
        assertNotNull(stats)
        assertEquals(14, stats.totalEvents)
        assertEquals(42, stats.totalRegistrations)
        assertEquals(2.8, stats.overallUtilizationPct, 0.01)
        assertEquals(5, stats.eventsByCategory["Cultural"])
    }
}
