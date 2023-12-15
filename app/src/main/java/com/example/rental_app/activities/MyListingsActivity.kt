package com.example.rental_app.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rental_app.MainActivity
import com.example.rental_app.R
import com.example.rental_app.adapters.MyListingAdapter
import com.example.rental_app.databinding.ActivityMyListingsBinding
import com.example.rental_app.models.Listings
import com.example.rental_app.models.User
import com.example.rental_app.repositories.PropertiesRepository
import com.example.rental_app.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class MyListingsActivity : AppCompatActivity() {
    private  val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityMyListingsBinding
    private lateinit var adapter: MyListingAdapter
    private var listingsList = listOf<Listings>()
    private lateinit var myListings: MutableList<Listings>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var propertiesRepository: PropertiesRepository
    private lateinit var userRepository: UserRepository
    private var loggedInUserEmail: String? = ""
    private lateinit var firebaseAuth: FirebaseAuth

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                lifecycleScope.launch{
                    val userData: User? = userRepository.getLoggedInUser(loggedInUserEmail)
                    Log.d(TAG, "onStart: userData: ${userData?.listings}")
                    propertiesRepository.getUserListings(userData?.listings)
                }
                this.propertiesRepository.allListings?.observe(this) { receivedData ->
                    if (receivedData.isNotEmpty()) {
                        myListings.clear()
                        myListings.addAll(receivedData)
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.d(TAG, "onStart: No data received from observer")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        this.firebaseAuth = FirebaseAuth.getInstance()

        this.sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        propertiesRepository = PropertiesRepository(applicationContext)
        userRepository = UserRepository(applicationContext)

        loggedInUserEmail = sharedPreferences.getString("USER_EMAIL", null)

        myListings = mutableListOf()
        this.adapter = MyListingAdapter(myListings, {pos -> rowClicked(pos)}, {pos -> editClickHandler(pos)})
        binding.rv.adapter = adapter

        binding.rv.layoutManager = LinearLayoutManager(this)

        binding.rv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val userData: User? = userRepository.getLoggedInUser(loggedInUserEmail)
            Log.d(TAG, "onStart: userData: ${userData?.listings}")
            propertiesRepository.getUserListings(userData?.listings)
        }

        propertiesRepository.allListings?.observe(this) { receivedData ->
            if (receivedData.isNotEmpty()) {
                myListings.clear()
                myListings.addAll(receivedData)
                adapter.notifyDataSetChanged()
            } else {
                Log.d(TAG, "onStart: No data received from observer")
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

    fun rowClicked(position:Int){
        val descriptionIntent = Intent(this, ResultDescriptionActivity::class.java)
        descriptionIntent.putExtra("PROPERTY_ID", myListings[position].id)
        descriptionIntent.putExtra("SOURCE", "MyListing")
        startActivity(descriptionIntent)
    }

    private fun editClickHandler(position: Int) {
        val editIntent = Intent(this, EditListingActivity::class.java)
        editIntent.putExtra("PROPERTY_ID", myListings[position].id)

        startForResult.launch(editIntent)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch{
            val userData: User? = userRepository.getLoggedInUser(loggedInUserEmail)
            Log.d(TAG, "onStart: userData: ${userData?.listings}")
            propertiesRepository.getUserListings(userData?.listings)
        }
        this.propertiesRepository.allListings?.observe(this) { receivedData ->
            if (receivedData.isNotEmpty()) {
                myListings.clear()
                myListings.addAll(receivedData)
                adapter.notifyDataSetChanged()
            } else {
                Log.d(TAG, "onStart: No data received from observer")
            }
        }
    }

}