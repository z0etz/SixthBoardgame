package com.katja.sixthboardgame

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.katja.sixthboardgame.databinding.ListItemGameBinding

class WelcomeAdapterCurrentGamesList(private val context: Context, private val dataList: List<String>, private val onItemClick: (String) -> Unit)  : RecyclerView.Adapter<WelcomeAdapterCurrentGamesList.ViewHolder>() {

    // ViewHolder class to hold references to the views in the item layout using View Binding
    class ViewHolder(private val binding: ListItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, data: String, onItemClick: (String) -> Unit) {
            binding.gamelistOpponent.text = context.getString(R.string.opponent_not_loaded)
            binding.gamelistTimeLeft.text = context.getString(R.string.time_not_loaded)
            itemView.setOnClickListener {
                onItemClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, dataList[position], onItemClick)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}