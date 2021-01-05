package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.main.LoadingApiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject


class AsteroidRepository (private val database: AsteroidDatabase){

    private val nextSevenDaysFormattedDates = getNextSevenDaysFormattedDates()



    var asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getOneDayAsteroids(
            nextSevenDaysFormattedDates.first())){it.asDomainModel() }

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
}