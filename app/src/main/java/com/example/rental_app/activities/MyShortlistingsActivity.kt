package com.example.rental_app.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rental_app.MainActivity
import com.example.rental_app.R
import com.example.rental_app.adapters.MyShortlistingsAdapter
import com.example.rental_app.databinding.ActivityMyShortlistingsBinding
import com.example.rental_app.models.Listings
import com.example.rental_app.models.User
import com.example.rental_app.repositories.PropertiesRepository
import com.example.rental_app.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class MyShortlistingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMyShortlistingsBinding
    private lateinit var adapter: MyShortlistingsAdapter
    private lateinit var prefEditor: SharedPreferences.Editor
    private var loggedUserObject: User? = User("", "", "", "", "", "")
    private lateinit var userRepository: UserRepository
    private lateinit var propertiesRepository: PropertiesRepository
    private lateinit var myListings: MutableList<Listings>
    private val TAG = javaClass.canonicalName
    private var loggedInUserEmail: String? = ""
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyShortlistingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        this.firebaseAuth = FirebaseAuth.getInstance()

        userRepository = UserRepository(applicationContext)
        propertiesRepository = PropertiesRepository(applicationContext)

        loggedInUserEmail = sharedPreferences.getString("USER_EMAIL", null)

        myListings = mutableListOf()

        adapter = MyShortlistingsAdapter(myListings, { pos -> removeListingAndUpdate(pos)}, {pos -> rowClicked(pos)})
        binding.rv.adapter = adapter

        binding.rv.layoutManager = LinearLayoutManager(this)

        binding.rv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )


        if (loggedInUserEmail != "") {
            lifecycleScope.launch{
                loggedUserObject = userRepository.getLoggedInUser(loggedInUserEmail)
                Log.d(TAG, "onStart: userData: ${loggedUserObject?.shortListing}")
                propertiesRepository.getUserFavourites(loggedUserObject?.shortListing)
            }

        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch{
            val userData: User? = userRepository.getLoggedInUser(loggedInUserEmail)
            Log.d(TAG, "onStart: userData: ${userData?.shortListing}")
            propertiesRepository.getUserFavourites(userData?.shortListing)
        }
        this.propertiesRepository.shortListings?.observe(this) { receivedData ->
            if (receivedData.isNotEmpty()) {
                myListings.clear()
                myListings.addAll(receivedData)
                adapter.notifyDataSetChanged()
            } else {
                Log.d(TAG, "onStart: No data received from observer")
            }
        }
    }


    private fun removeListingAndUpdate(position: Int) {
        lifecycleScope.launch{
            val firebaseListingId = propertiesRepository.getPropertyIdById(myListings[position].id)
            propertiesRepository.removeUserFavourites(loggedInUserEmail, firebaseListingId)

            val userData: User? = userRepository.getLoggedInUser(loggedInUserEmail)
            Log.d(TAG, "removeListingAndUpdate: userData: ${userData?.shortListing}")
            propertiesRepository.getUserFavourites(userData?.shortListing)
        }

        this.propertiesRepository.shortListings?.observe(this) { receivedData ->
            if (receivedData.isNotEmpty()) {
                myListings.clear()
                myListings.addAll(receivedData)
                adapter.notifyDataSetChanged()
            } else {
                Log.d(TAG, "removeListingAndUpdate: No data received from observer")
            }
        }
    }

    fun rowClicked(position:Int){
        val detailIntent = Intent(this, DetailActivity::class.java)
        detailIntent.putExtra("PROPERTY_ID", myListings[position].id)
        detailIntent.putExtra("SOURCE", "MyListing")
        startActivity(detailIntent)
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