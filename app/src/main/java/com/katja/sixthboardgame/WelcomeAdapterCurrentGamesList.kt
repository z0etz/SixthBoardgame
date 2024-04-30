package com.katja.sixthboardgame

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.katja.sixthboardgame.databinding.ListItemGameBinding

class WelcomeAdapterCurrentGamesList(private val dataList: List<String>)  : RecyclerView.Adapter<WelcomeAdapterCurrentGamesList.ViewHolder>() {

    // ViewHolder class to hold references to the views in the item layout using View Binding
    class ViewHolder(private val binding: ListItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: String) {
           //TODO: Figure out how to access string resources from adapter, then extract them.
            binding.gamelistOpponent.text = "Opponent not loaded"
            binding.gamelistTimeLeft.text = "Time not loaded"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}