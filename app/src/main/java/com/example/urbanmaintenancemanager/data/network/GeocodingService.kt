package com.example.urbanmaintenancemanager.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.osmdroid.util.GeoPoint

@Serializable
data class NominatimResponse(
    @SerialName("place_id")
    val placeId: Long,
    val licence: String,
    @SerialName("osm_type")
    val osmType: String,
    @SerialName("osm_id")
    val osmId: Long,
    val lat: String,
    val lon: String,
    @SerialName("display_name")
    val displayName: String,
    val address: Address
)

@Serializable
data class Address(
    val road: String? = null,
    val suburb: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state: String? = null,
    @SerialName("postcode")
    val postcode: String? = null,
    val country: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null
)

object GeocodingService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getAddressFromCoordinates(geoPoint: GeoPoint): String? {
        return try {
            val response: NominatimResponse = client.get("https://nominatim.openstreetmap.org/reverse") {
                parameter("format", "jsonv2")
                parameter("lat", geoPoint.latitude)
                parameter("lon", geoPoint.longitude)
                parameter("accept-language", "fa") // Request address in Persian
            }.body()
            response.displayName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 