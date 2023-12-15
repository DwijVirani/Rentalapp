package com.example.rental_app.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.rental_app.MainActivity
import com.example.rental_app.R
import com.example.rental_app.databinding.ActivityDetailBinding
import com.example.rental_app.models.Listings
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        this.sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        val listingJson = intent.getStringExtra("LISTING_DETAILS")
        val listing = Gson().fromJson(listingJson, Listings::class.java)

        this.firebaseAuth = FirebaseAuth.getInstance()

        val imageViewListingDetail = findViewById<ImageView>(R.id.imageViewListingDetail)
        val tvPropertyType = findViewById<TextView>(R.id.tvDetailPropertyType)
        val tvBedrooms = findViewById<TextView>(R.id.tvDetailBedrooms)
        val tvKitchen = findViewById<TextView>(R.id.tvDetailKitchen)
        val tvBathroom = findViewById<TextView>(R.id.tvDetailBathroom)
        val tvDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val tvAddress = findViewById<TextView>(R.id.tvDetailAddress)
        val tvAvailability = findViewById<TextView>(R.id.tvDetailAvailability)
        val tvBuildingName = findViewById<TextView>(R.id.tvDetailBuildingName)
        val tvPostalCode = findViewById<TextView>(R.id.tvDetailPostalCode)
        val tvProvince = findViewById<TextView>(R.id.tvDetailProvince)
        val tvCity = findViewById<TextView>(R.id.tvDetailCity)
        val tvRent = findViewById<TextView>(R.id.tvDetailRent)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val resId = resources.getIdentifier(listing.img, "drawable", packageName)
        imageViewListingDetail.setImageResource(resId)

        tvPropertyType.text = listing.propertyType
        tvBedrooms.text = "Bedrooms: ${listing.bedrooms}"
        tvKitchen.text = "Kitchen: ${listing.kitchen}"
        tvBathroom.text = "Bathroom: ${listing.bathroom}"
        tvDescription.text = listing.description
        tvAddress.text = listing.address
        tvAvailability.text = if (listing.isAvailable) "Available" else "Not Available"
        tvBuildingName.text = listing.buildingName
        tvPostalCode.text = listing.postalCode
        tvProvince.text = listing.province
        tvCity.text = listing.city
        tvRent.text = "Rent: ${listing.rent}"


        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){
            R.id.menuMyListings -> {
                val myListingIntent = Intent(this, MyListingsActivity::class.java)
                startActivity(myListingIntent)
                return true
            }
            R.id.menuPostRental -> {
                val postRentalIntent = Intent(this, PostRentalActivity::class.java)
                startActivity(postRentalIntent)
                return true
            }
            R.id.menuLogout -> {
                prefEditor.remove("USER_EMAIL")
                prefEditor.remove("USER_PASSWORD")
                prefEditor.apply()
                this.firebaseAuth.signOut()
                startActivity(Intent(this, MainActivity::class.java))

                return  true
            }
            R.id.menuHome -> {
                startActivity(Intent(this, MainActivity::class.java))
                return  true
            }
            R.id.menuShortList -> {
                startActivity(Intent(this, MyShortlistingsActivity::class.java))
                return  true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}