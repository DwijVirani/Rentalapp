package com.example.rental_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.rental_app.MainActivity
import com.example.rental_app.R
import com.example.rental_app.databinding.ActivityInquiryBinding
import com.example.rental_app.models.Listings
import com.example.rental_app.repositories.PropertiesRepository
import com.example.rental_app.repositories.UserRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch

class InquiryActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityInquiryBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var propertiesRepository: PropertiesRepository
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        this.firebaseAuth = FirebaseAuth.getInstance()

        propertiesRepository = PropertiesRepository(applicationContext)
        userRepository = UserRepository(applicationContext)

        this.sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        if(intent != null) {
            val propertyId = intent.getStringExtra("PROPERTY_ID")
            lifecycleScope.launch {
                val propertyData = propertiesRepository.getPropertyById(propertyId)
                val userData = userRepository.getLoggedByEmail(propertyData?.owner)

                binding.etFirstName.setText(userData?.firstName)
                binding.etLastName.setText(userData?.lastName)
                binding.etEmail.setText(userData?.email)
                binding.etPhone.setText(userData?.phoneNumber)
            }
        }

        binding.btnSendInquiry.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btnSendInquiry -> {
                this.sendInquiry()
            }
        }
    }

    private fun sendInquiry() {
        if(intent != null) {
            val propertyId = intent.getStringExtra("PROPERTY_ID")
            lifecycleScope.launch {
                val propertyData = propertiesRepository.getPropertyById(propertyId)
                val userData = userRepository.getLoggedByEmail(propertyData?.owner)

                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    data = Uri.parse("mailto: ${userData?.email}")
                    putExtra(Intent.EXTRA_EMAIL, userData?.email)
                    putExtra(Intent.EXTRA_SUBJECT, "You have new inquiry for your listing")
                    putExtra(Intent.EXTRA_TEXT, binding.etMessage.text.toString())

                }
                if (emailIntent.resolveActivity(packageManager) != null){
                    startActivity(emailIntent)
                }

                Snackbar.make(binding.rootLayout, "Inquiry Sent", Snackbar.LENGTH_SHORT).show()
                Handler().postDelayed({
                    finish()
                }, 2000)
            }
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