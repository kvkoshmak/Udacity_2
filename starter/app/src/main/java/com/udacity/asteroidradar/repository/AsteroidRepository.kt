package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class AsteroidRepository (private val database: AsteroidDatabase){

    private val nextSevenDaysFormattedDates = getNextSevenDaysFormattedDates()

    var asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids(
            )){it.asDomainModel() }

    val asteroidsDay: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getOneDayAsteroids(
            nextSevenDaysFormattedDates.first())) { it.asDomainModel() }

    val asteroidsRange = Transformations.map(database.asteroidDao.getRangeAsteroids(
            nextSevenDaysFormattedDates.first(), nextSevenDaysFormattedDates.last())) { it.asDomainModel() }


    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            val jsonResult = Network.retrofitService.getAsteroidList()
            // upload asteroid list using parser
            val netAsteroids = NetworkAsteroidContainer(parseAsteroidsJsonResult(JSONObject(jsonResult)))
            database.asteroidDao.insertAll(*netAsteroids.asDatabaseModel())
        }
    }
    suspend fun deleteYesterdayAsteroids() {
        withContext(Dispatchers.IO) {
            database.asteroidDao.deleteYesterday(getYesterday())
        }
    }

    suspend fun refreshTodayAsteroids() {
        withContext(Dispatchers.IO) {
            val jsonResult = Network.retrofitService.getAsteroidListToday()
            // upload asteroid list using parser
            val netAsteroids = NetworkAsteroidContainer(parseAsteroidsJsonResult(JSONObject(jsonResult)))
            database.asteroidDao.insertAll(*netAsteroids.asDatabaseModel())
        }
    }

    fun getYesterday() : String {
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }
}