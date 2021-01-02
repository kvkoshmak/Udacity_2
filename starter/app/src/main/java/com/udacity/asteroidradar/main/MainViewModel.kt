package com.udacity.asteroidradar.main

import android.accounts.AccountManager.get
import android.content.Context
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.reflect.Array.get

enum class loadingApiStatus { LOADING, ERROR, DONE }

class MainViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<loadingApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<loadingApiStatus>
        get() = _status

    // Image of the day data
    private val _dailyPicture = MutableLiveData<PictureOfDay>()
    val dailyPicture: LiveData<PictureOfDay>
        get() = _dailyPicture

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
            val jsonResult = AsteroidApi.retrofitService.getAsteroidList()
            val js = JSONObject(jsonResult)
            // upload asteroid list using parser
            _asteroids.value = parseAsteroidsJsonResult(js)
            //load image of the day
            _dailyPicture.value = AsteroidApi.retrofitService.getImageOfTheDay()
            // status check
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