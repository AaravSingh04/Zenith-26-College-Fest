package com.zenith26.fest.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton API Client managing Retrofit with dynamically configurable Base URL.
 */
object ApiClient {

    private const val PREFS_NAME = "zenith26_api_prefs"
    private const val KEY_BASE_URL = "base_url"

    // Public Cloudflare Tunnel for cloud emulators (Appetize.io) and any physical phone
    const val DEFAULT_TUNNEL_URL = "https://widely-lawrence-george-wax.trycloudflare.com/"
    // Default host for Android Emulator connecting to host machine
    const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:8080/"
    // Preset for current dev machine's LAN address
    const val DEFAULT_LAN_URL = "http://172.16.51.33:8080/"
    // Preset for localhost
    const val DEFAULT_LOCALHOST_URL = "http://127.0.0.1:8080/"

    private var prefs: SharedPreferences? = null
    private var currentBaseUrl: String = DEFAULT_TUNNEL_URL

    private val gson = Gson()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private var retrofit: Retrofit? = null
    private var apiService: ZenithApiService? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentBaseUrl = prefs?.getString(KEY_BASE_URL, DEFAULT_TUNNEL_URL) ?: DEFAULT_TUNNEL_URL
        rebuildRetrofit()
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun setBaseUrl(newUrl: String) {
        var formatted = newUrl.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            formatted = "http://$formatted"
        }
        if (!formatted.endsWith("/")) {
            formatted = "$formatted/"
        }
        currentBaseUrl = formatted
        prefs?.edit()?.putString(KEY_BASE_URL, formatted)?.apply()
        rebuildRetrofit()
    }

    private fun rebuildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        apiService = retrofit?.create(ZenithApiService::class.java)
    }

    fun getService(): ZenithApiService {
        if (apiService == null) {
            rebuildRetrofit()
        }
        return apiService!!
    }

    /**
     * Parse error message from FastAPI response body (e.g. `{"detail": "..."}`)
     */
    fun parseErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val json = gson.fromJson(errorBody, JsonObject::class.java)
                if (json.has("detail")) {
                    val detail = json.get("detail")
                    if (detail.isJsonPrimitive) {
                        return detail.asString
                    } else if (detail.isJsonArray) {
                        // FastAPI validation error array
                        val first = detail.asJsonArray.firstOrNull()?.asJsonObject
                        val msg = first?.get("msg")?.asString
                        if (!msg.isNullOrBlank()) return msg
                    }
                }
                return errorBody
            }
            "Server error: HTTP ${response.code()}"
        } catch (e: Exception) {
            "Server responded with error ${response.code()}"
        }
    }
}
