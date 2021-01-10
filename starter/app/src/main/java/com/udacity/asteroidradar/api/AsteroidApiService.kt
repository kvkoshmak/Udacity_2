package com.udacity.asteroidradar.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.Constants.BASE_URL
import com.udacity.asteroidradar.PictureOfDay
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
val days = getNextSevenDaysFormattedDates()

val TODAY = days.first()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

interface AsteroidApiService {

    @GET("neo/rest/v1/feed?&api_key=${API_KEY}")
    suspend fun getAsteroidList(): String

//    @GET("neo/rest/v1/feed?start_date=${today}&end_date=${today}&api_key=${API_KEY}")
//    suspend fun getAsteroidListToday(): String
    @GET("neo/rest/v1/feed?start_date={today}&end_date={today}&api_key=${API_KEY}")
    suspend fun getAsteroidListToday(@Path("today") date: String = TODAY): String


    @GET("planetary/apod?api_key=${API_KEY}")
    suspend fun getImageOfTheDay(): PictureOfDay
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object Network {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    val retrofitService : AsteroidApiService = retrofit.create(AsteroidApiService::class.java)
}
