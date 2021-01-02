package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

enum class loadingApiStatus { LOADING, ERROR, DONE }

class MainViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<loadingApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<loadingApiStatus>
        get() = _status

    private val _asteroids = MutableLiveData<List<Asteroid>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

    // Internally, we use a MutableLiveData to handle navigation to the selected property
    private val _navigateToSelectedProperty = MutableLiveData<Asteroid>()

    // The external immutable LiveData for the navigation property
    val navigateToSelectedProperty: LiveData<Asteroid>
        get() = _navigateToSelectedProperty

    init {
        viewModelScope.launch {
            returnAsteroids()
        }

    }

    suspend fun returnAsteroids(){
        _status.value = loadingApiStatus.LOADING
        try {
            val jsonResult = AsteroidApi.retrofitService.getProperties()
            val js = JSONObject(jsonResult)
            _asteroids.value = parseAsteroidsJsonResult(js)
            _status.value = loadingApiStatus.DONE
        } catch (e: Exception) {
            _status.value = loadingApiStatus.ERROR
            _asteroids.value = ArrayList()
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
}