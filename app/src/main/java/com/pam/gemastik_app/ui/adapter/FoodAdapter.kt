package com.pam.gemastik_app.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pam.gemastik_app.databinding.RowMakananBinding
import com.pam.gemastik_app.model.FoodModel
import com.pam.gemastik_app.ui.FoodDetailActivity

class FoodAdapter(private val context: Context, private val food: List<FoodModel>) : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: RowMakananBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(food: FoodModel) {
            binding.tvBreaky.text = food.namaMenu
            binding.tvCalBreaky.text = "${food.KALORIMAKANAN} kkal"
            Glide.with(context)
                .load(food.foto)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true))
                .into(binding.ivBreaky)
            binding.clBreaky.setOnClickListener {
                val intent = Intent(it.context, FoodDetailActivity::class.java)
                intent.putExtra("bmenu", food.namaMenu)
                intent.putExtra("bdesc", food.description)
                intent.putExtra("bingredient", food.ingredients)
                intent.putExtra("bfoto", food.foto)
                it.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodAdapter.ViewHolder {
        val binding = RowMakananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodAdapter.ViewHolder, position: Int) {
        holder.bind(food[position])
    }

    override fun getItemCount(): Int {
        return food.size
    }
}