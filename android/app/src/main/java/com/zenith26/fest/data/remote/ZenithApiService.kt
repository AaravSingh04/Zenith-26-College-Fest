package com.zenith26.fest.data.remote

import com.zenith26.fest.data.model.ApiMessage
import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.EventCreateRequest
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.model.RegistrationResponse
import com.zenith26.fest.data.model.StatsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface mapping all existing FastAPI endpoints directly.
 */
interface ZenithApiService {

    @GET("/api/events")
    suspend fun getEvents(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<List<Event>>

    @GET("/api/events/{event_id}")
    suspend fun getEvent(
        @Path("event_id") eventId: Int
    ): Response<Event>

    @POST("/api/events/{event_id}/register")
    suspend fun registerForEvent(
        @Path("event_id") eventId: Int,
        @Body request: RegistrationRequest
    ): Response<RegistrationResponse>

    @GET("/api/registrations")
    suspend fun getRegistrations(
        @Query("event_id") eventId: Int? = null
    ): Response<List<RegistrationDetail>>

    @GET("/api/stats")
    suspend fun getStats(): Response<StatsResponse>

    // Stretch Goal: Admin adds event
    @POST("/api/events")
    suspend fun createEvent(
        @Body request: EventCreateRequest
    ): Response<Event>

    // Stretch Goal: Admin cancels registration
    @DELETE("/api/registrations/{registration_id}")
    suspend fun deleteRegistration(
        @Path("registration_id") registrationId: Int
    ): Response<ApiMessage>
}
