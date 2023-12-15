package com.example.rental_app.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rental_app.activities.DetailActivity
import com.example.rental_app.R
import com.example.rental_app.models.Listings
import com.google.gson.Gson

class MyShortlistingsAdapter(
    private var myListings: MutableList<Listings>,
    private val onItemRemoved: (Int) -> Unit,
    private val rowClickHandler:(Int) -> Unit
) : RecyclerView.Adapter<MyShortlistingsAdapter.MyShortlistingsViewHolder>() {
    private val gson = Gson()

    inner class MyShortlistingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            val btnRemove = itemView.findViewById<Button>(R.id.btnRemoveListing)
            btnRemove.setOnClickListener {
                onItemRemoved(adapterPosition)
            }

            itemView.setOnClickListener {
                rowClickHandler(adapterPosition)
            }
        }

        val tvPropertyType: TextView = itemView.findViewById(R.id.tvPropertyType)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvRent: TextView = itemView.findViewById(R.id.tvRent)
        val imageViewListing: ImageView = itemView.findViewById(R.id.imageViewListing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyShortlistingsViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_layout_my_shortlistings, parent, false)
        return MyShortlistingsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myListings.size
    }

    override fun onBindViewHolder(holder: MyShortlistingsViewHolder, position: Int) {
        val listing = myListings[position]
        holder.tvPropertyType.text = listing.propertyType
        holder.tvDescription.text = listing.description
        holder.tvAddress.text = listing.address
        holder.tvRent.text = "Rent: ${listing.rent}"

        val context = holder.itemView.context
        val resId = context.resources.getIdentifier(listing.img, "drawable", context.packageName)

        holder.imageViewListing.setImageResource(resId)
    }
}

