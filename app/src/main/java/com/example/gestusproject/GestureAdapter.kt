package com.example.gestusproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gestusproject.databinding.ItemGestureBinding

class GestureAdapter(
    private val gestures: List<HomeActivity.Gesture>,
    private val onItemClick: (HomeActivity.Gesture) -> Unit
) : RecyclerView.Adapter<GestureAdapter.ViewHolder>() {
    
    class ViewHolder(val binding: ItemGestureBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGestureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gesture = gestures[position]
        with(holder.binding) {
            tvTitle.text = gesture.title
            tvDescription.text = gesture.description
            ivGesture.setImageResource(gesture.imageRes)
            
            root.setOnClickListener {
                onItemClick(gesture)
            }
        }
    }
    
    override fun getItemCount() = gestures.size
}

