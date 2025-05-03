package com.example.slt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SignAdapter(private val imageUrls: List<String>):
    RecyclerView.Adapter<SignAdapter.SignViewHolder>(){

        class SignViewHolder(view: View): RecyclerView.ViewHolder(view){
            val signImage: ImageView = view.findViewById(R.id.signImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sign_image, parent, false)
            return SignViewHolder(view)
        }

        override fun onBindViewHolder(holder: SignViewHolder, position: Int){
            Glide.with(holder.signImage.context)
                .load(imageUrls[position])
                .into(holder.signImage)
        }

        override fun getItemCount() = imageUrls.size
    }