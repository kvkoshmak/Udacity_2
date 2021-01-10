package com.udacity.asteroidradar.main

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy{
    val activity = requireNotNull(this.activity) {
        "You can only access the viewModel after onViewCreated()"
    }
    ViewModelProvider(this, MainViewModel.Factory(activity.application)).get(MainViewModel::class.java)

}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.asteroidRecycler.adapter = AsteroidListAdapter(AsteroidListAdapter.OnClickListener {
            viewModel.displayPropertyDetails(it)
        })

        viewModel.navigateToSelectedProperty.observe(this, Observer {
            if ( null != it ) {
                // Must find the NavController from the Fragment
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayPropertyDetailsComplete()
            }
        })


        viewModel.dailyPicture.observe(this, Observer {
            it?.let{
                val sharedPref = activity?.getSharedPreferences("my_pref",Context.MODE_PRIVATE)
                with (sharedPref!!.edit()) {
                    putString("new_url", viewModel.dailyPicture.value?.url)
                    //printout the name
//                    Toast.makeText(activity, viewModel.dailyPicture.value?.url, Toast.LENGTH_SHORT).show()
                    apply()
                }
            }
        })



        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.updateFilter(
                when (item.itemId) {
                    R.id.show_week_asteroids -> ApiFilter.SHOW_WEEK
                    R.id.show_today_asteroids -> ApiFilter.SHOW_TODAY
                    else -> ApiFilter.SHOW_ALL
                }
        )
        return true
    }
}
