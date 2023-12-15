package com.example.rental_app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rental_app.R
import com.example.rental_app.models.Listings

class MyListingAdapter(
    private var myListings: List<Listings>,
    private val rowClickHandler:(Int) -> Unit,
    private val editClickHandler: (Int) -> Unit
) : RecyclerView.Adapter<MyListingAdapter.MyListingViewHolder>() {
    inner class MyListingViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) {
        init {
            itemView.setOnClickListener {
                rowClickHandler(adapterPosition)
            }
            val btnEdit = itemView.findViewById<ImageButton>(R.id.btnEdit)
            btnEdit.setOnClickListener {
                editClickHandler(adapterPosition)
            }
        }
        val buildName: TextView = itemView.findViewById(R.id.tvRowLine1)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvRent: TextView = itemView.findViewById(R.id.tvRent)
        val img: ImageView = itemView.findViewById(R.id.ivCardImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyListingViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.row_layout_my_listings, parent, false)
        return MyListingViewHolder(view)
    }
    override fun getItemCount(): Int {
        return myListings.size
    }

    override fun onBindViewHolder(holder: MyListingViewHolder, position: Int) {
        val listing = myListings[position]
        Log.d("onBindViewHolder", "onBindViewHolder: listings: $listing")
        holder.buildName.text = listing.buildingName
        holder.tvDescription.text = "Description${ listing.description }"
        holder.tvAddress.text = "Address: ${listing.address}"
        holder.tvRent.text = "Rent: ${listing.rent}"
        val context = holder.itemView.context
        val res = context.resources.getIdentifier(myListings.get(position).img, "drawable", context.packageName)
        holder.img.setImageResource(res)
    }

}