package com.pam.gemastik_app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pam.gemastik_app.databinding.RowRecentfoodBinding
import com.pam.gemastik_app.model.RecentFoodsModel

class RecentFoodsAdapter(private val context: Context, private val food: List<RecentFoodsModel>) : RecyclerView.Adapter<RecentFoodsAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: RowRecentfoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: RecentFoodsModel) {
            binding.tvRecentFoods.text = food.menu
            binding.tvRecentFoodCalories.text = food.calorie
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentFoodsAdapter.ViewHolder {
        val binding = RowRecentfoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentFoodsAdapter.ViewHolder, position: Int) {
        holder.bind(food[position])
    }

    override fun getItemCount(): Int {
        return food.size
    }
}