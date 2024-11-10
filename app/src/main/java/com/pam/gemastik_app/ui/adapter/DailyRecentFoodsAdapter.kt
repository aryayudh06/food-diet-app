package com.pam.gemastik_app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pam.gemastik_app.databinding.RowDailyrecentfoodBinding
import com.pam.gemastik_app.model.DailyRecentFoodsModel

class DailyRecentFoodsAdapter(private val context: Context, private var food: List<DailyRecentFoodsModel>) : RecyclerView.Adapter<DailyRecentFoodsAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: RowDailyrecentfoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dailyFood: DailyRecentFoodsModel) {
            binding.tvDailyFoodsDate.text = dailyFood.date
            val foodAdapter = RecentFoodsAdapter(context, dailyFood.foods)
            binding.rvDailyFoods.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.rvDailyFoods.adapter = foodAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowDailyrecentfoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = food.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(food[position])
    }

    fun updateData(newData: List<DailyRecentFoodsModel>) {
        food = newData
        notifyDataSetChanged()
    }
}