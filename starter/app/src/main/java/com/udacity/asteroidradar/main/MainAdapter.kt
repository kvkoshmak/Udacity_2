package com.udacity.asteroidradar.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.databinding.LviewItemBinding

class AsteroidListAdapter( val onClickListener: OnClickListener ) :
    ListAdapter<Asteroid, AsteroidListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private var binding: LviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(marsProperty: Asteroid) {
            binding.asteroid = marsProperty
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Asteroid>() {
        override fun areItemsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Asteroid, newItem: Asteroid): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AsteroidListAdapter.ViewHolder {
        return ViewHolder(LviewItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(
        holder: AsteroidListAdapter.ViewHolder,
        position: Int
    ) {
        val asteroidData = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(asteroidData)
        }
        holder.bind(asteroidData)
    }

    class OnClickListener(val clickListener: (asteroidData:Asteroid) -> Unit) {
        fun onClick(asteroidData:Asteroid) = clickListener(asteroidData)
    }

}
