package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception


enum class LoadingApiStatus { LOADING, ERROR, DONE }

class MainViewModel (application: Application) : AndroidViewModel(application) {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<LoadingApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<LoadingApiStatus>
        get() = _status

    // Image of the day data
    private val _dailyPicture = MutableLiveData<PictureOfDay>()
    val dailyPicture: LiveData<PictureOfDay>
        get() = _dailyPicture

    // Internally, we use a MutableLiveData to handle navigation to the selected property
    private val _navigateToSelectedProperty = MutableLiveData<Asteroid>()

    // The external immutable LiveData for the navigation property
    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedProperty

    //set database
    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)


    init {
        viewModelScope.launch {
        refreshDataFromNetwork()
        }
    }
    val asteroids = asteroidRepository.asteroids

    private suspend fun refreshDataFromNetwork() {
        _status.value = LoadingApiStatus.LOADING
        try {
            //refresh asteroids
            val jsonResult = Network.retrofitService.getAsteroidList()
            // upload asteroid list using parser
            val jsonObject = JSONObject(jsonResult)
            val res = parseAsteroidsJsonResult(jsonObject)
            val netAsteroids = NetworkAsteroidContainer(res)
            database.asteroidDao.insertAll(*netAsteroids.asDatabaseModel())
            //refresh image
            _dailyPicture.value = Network.retrofitService.getImageOfTheDay().await()
            // status check
            _status.value = LoadingApiStatus.DONE
        } catch (e: Exception) {
            _status.value = LoadingApiStatus.ERROR
        }
    }

    fun displayPropertyDetails(marsProperty: Asteroid) {
        _navigateToSelectedProperty.value = marsProperty
    }

    /**
     * After the navigation has taken place, navigateToSelectedProperty is set to null
     */
    fun displayPropertyDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}