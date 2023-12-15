package com.example.rental_app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rental_app.R
import com.example.rental_app.models.Listings

class ResultsAdaptor(
    private val results:MutableList<Listings>,
    private val rowClickHandler:(Int) -> Unit,
    private val callButtonClickHandler: (Int) -> Unit,
    private val enquiryButtonHandler:(Int) -> Unit):RecyclerView.Adapter<ResultsAdaptor.ResultsViewHolder>() {

    inner class ResultsViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) {
        init {
            itemView.setOnClickListener {
                rowClickHandler(adapterPosition)
            }
            itemView.findViewById<Button>(R.id.enquiry).setOnClickListener {
                enquiryButtonHandler(adapterPosition)
            }
            itemView.findViewById<Button>(R.id.call).setOnClickListener {
                callButtonClickHandler(adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.card_layout_results, parent, false)
        return ResultsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        Log.d("getUserListings2", "onBindViewHolder: $results")

        val res_obj = results.get(position)
        val tv = holder.itemView.findViewById<TextView>(R.id.cost)
        tv.text = "$${res_obj.rent}/-"

        val desc = holder.itemView.findViewById<TextView>(R.id.property_description)
        desc.text = "${res_obj.bedrooms}Bed|${res_obj.bathroom}Bath|\n" +
                "${res_obj.kitchen}Kitchen"
        val cityprovince = holder.itemView.findViewById<TextView>(R.id.city_province)
        cityprovince.text = "${res_obj.city},${res_obj.province}"
        val context = holder.itemView.context
        val res = context.resources.getIdentifier(results.get(position).img, "drawable", context.packageName)
        val iv = holder.itemView.findViewById<ImageView>(R.id.card_img)
        iv.setImageResource(res)
    }
}