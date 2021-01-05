package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import java.lang.Exception


enum class LoadingApiStatus { LOADING, ERROR, DONE }
enum class ApiFilter { SHOW_TODAY, SHOW_WEEK, SHOW_ALL}

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

    // set filter for asteroid request
    private val apiFilter = MutableLiveData<ApiFilter>(ApiFilter.SHOW_ALL)

    //set database
    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)


    init {
        viewModelScope.launch {
        refreshDataFromNetwork()
        }
    }
    // set the filter value and choose slice of dataset
    val asteroids =Transformations.switchMap(apiFilter){
        when(it){
            ApiFilter.SHOW_TODAY -> asteroidRepository.asteroidsDay
            ApiFilter.SHOW_WEEK -> asteroidRepository.asteroidsRange
            else -> asteroidRepository.asteroids
        }
    }


    private suspend fun refreshDataFromNetwork() {
        _status.value = LoadingApiStatus.LOADING
        try {
            //refresh image
            _dailyPicture.value = Network.retrofitService.getImageOfTheDay()
            //refresh asteroids
            asteroidRepository.refreshAsteroids()
            // status check
            _status.value = LoadingApiStatus.DONE
        } catch (e: Exception) {
            e.printStackTrace()
            _status.value = LoadingApiStatus.ERROR
        }
    }
    // start nav
    fun displayPropertyDetails(marsProperty: Asteroid) {
        _navigateToSelectedProperty.value = marsProperty
    }
    // finish nav
    fun displayPropertyDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }
    // update filter
    fun updateFilter(filter :ApiFilter){
        apiFilter.value = filter
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