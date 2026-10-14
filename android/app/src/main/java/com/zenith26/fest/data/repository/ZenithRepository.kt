package com.zenith26.fest.data.repository

import com.zenith26.fest.data.model.Event
import com.zenith26.fest.data.model.EventCreateRequest
import com.zenith26.fest.data.model.RegistrationDetail
import com.zenith26.fest.data.model.RegistrationRequest
import com.zenith26.fest.data.model.RegistrationResponse
import com.zenith26.fest.data.model.StatsResponse
import com.zenith26.fest.data.remote.ApiClient
import com.zenith26.fest.data.remote.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository mediating between ViewModels and the Remote API Service.
 */
class ZenithRepository {

    private val api get() = ApiClient.getService()

    suspend fun getEvents(category: String?, search: String?): NetworkResult<List<Event>> =
        withContext(Dispatchers.IO) {
            try {
                val catParam = if (category.isNullOrBlank() || category.equals("All", ignoreCase = true)) null else category
                val searchParam = if (search.isNullOrBlank()) null else search
                val response = api.getEvents(category = catParam, search = searchParam)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val msg = ApiClient.parseErrorMessage(response)
                    NetworkResult.Error(msg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Failed to connect to server. Ensure FastAPI is running.")
            }
        }

    suspend fun getEvent(eventId: Int): NetworkResult<Event> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getEvent(eventId)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val msg = ApiClient.parseErrorMessage(response)
                    NetworkResult.Error(msg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Failed to load event details")
            }
        }

    suspend fun registerForEvent(eventId: Int, request: RegistrationRequest): NetworkResult<RegistrationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerForEvent(eventId, request)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val msg = ApiClient.parseErrorMessage(response)
                    NetworkResult.Error(msg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error("Network error: Could not reach server. Verify host address and port.")
            }
        }

    suspend fun getRegistrations(eventId: Int? = null): NetworkResult<List<RegistrationDetail>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getRegistrations(eventId)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val msg = ApiClient.parseErrorMessage(response)
                    NetworkResult.Error(msg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Failed to fetch registrations")
            }
        }

    suspend fun getStats(): NetworkResult<StatsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getStats()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val msg = ApiClient.parseErrorMessage(response)
                    NetworkResult.Error(msg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Failed to load fest statistics")
            }
        }

    suspend fun createEvent(request: EventCreateRequest): NetworkResult<Event> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.createEvent(request)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    val msg = ApiClient.parseErrorMessage(response)
                    NetworkResult.Error(msg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Failed to create event")
            }
        }

    suspend fun deleteRegistration(registrationId: Int): NetworkResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.deleteRegistration(registrationId)
                if (response.isSuccessful) {
                    val msg = response.body()?.message ?: "Registration cancelled successfully"
                    NetworkResult.Success(msg)
                } else {
                    val msg = ApiClient.parseErrorMessage(response)
                    NetworkResult.Error(msg, response.code())
                }
            } catch (e: Exception) {
                NetworkResult.Error(e.localizedMessage ?: "Failed to cancel registration")
            }
        }
}
