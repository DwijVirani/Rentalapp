package com.example.rental_app

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.rental_app.activities.LocalResultsActivity
import com.example.rental_app.activities.LoginActivity
import com.example.rental_app.activities.MyListingsActivity
import com.example.rental_app.activities.MyShortlistingsActivity
import com.example.rental_app.activities.PostRentalActivity
import com.example.rental_app.activities.RegistrationActivity
import com.example.rental_app.activities.ResultsActivity
import com.example.rental_app.adapters.CitiesAdaptor
import com.example.rental_app.databinding.ActivityMainBinding
import com.example.rental_app.models.Listings
import com.example.rental_app.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var firebaseAuth: FirebaseAuth

    private var allPermissionsGrantedTracker = true



    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val APP_PERMISSIONS_LIST = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )



    private val multiplePermissionsResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {


            resultsList ->
        Log.d("getMultiplePermissions", resultsList.toString())



        for (item in resultsList.entries) {
            if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                allPermissionsGrantedTracker = false
            }
        }


        if (allPermissionsGrantedTracker == true) {
            var snackbar =
                Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_LONG)
            snackbar.show()
//            getDeviceLocation()


        } else {
            var snackbar =
                Snackbar.make(binding.root, "Some permissions NOT granted", Snackbar.LENGTH_LONG)
            snackbar.show()
            handlePermissionDenied()
        }


    }


    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location === null) {
                    Log.d("getLocation", "Location is null")
                    return@addOnSuccessListener
                }
                // Output the location
                val message =
                    "The device is located at: ${location.latitude}, ${location.longitude}"
//                binding.test.text = "${location.latitude},${location.longitude}"

            }
    }


    private fun handlePermissionDenied() {
        // output the rationale
        // disable the get device location button
//        binding.test.setText("Sorry, you need to give us permissions before we can get your location. Check your settings menu and update your location permissions for this app.")
        // disable the button
        multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
//        binding.btnGetDeviceLocation.isEnabled = false
    }

    val citiesList = listOf<String>("Toronto","Calgary","Ottawa","Regina")
    val TAG = "mainactivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        this.binding.register.setOnClickListener{registerClickHandler()}
        this.binding.postRental.setOnClickListener { postrentalClickHandler() }
        this.binding.login.setOnClickListener { loginClickHandler() }

        this.sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        prefEditor = this.sharedPreferences.edit()

        this.firebaseAuth = FirebaseAuth.getInstance()

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        binding.searchButton.setOnClickListener {
            if(binding.search.text.toString().isNullOrEmpty()){
                binding.searchError.isVisible = true
                return@setOnClickListener
            }
            SearchResults(binding.search.text.toString())
        }

        binding.nearByProperty.setOnClickListener {
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            if(allPermissionsGrantedTracker == true){
                SearchResultsNearMe()
            }
        }


        var adapter: CitiesAdaptor = CitiesAdaptor(citiesList,
            {pos -> buttonClicked(pos) })
        binding.rvCities.adapter = adapter
        binding.rvCities.layoutManager = GridLayoutManager(this, 2)
    }

    override fun onResume() {
        super.onResume()
        binding.searchError.isVisible = false
    }
    fun SearchResults(searchval:String){
        val ResultsIntent = Intent(this@MainActivity, ResultsActivity::class.java)
        ResultsIntent.putExtra("SEARCH_VAL",searchval)
        startActivity(ResultsIntent)
    }
    fun SearchResultsNearMe(){
        val NearByResultsIntent = Intent(this@MainActivity, LocalResultsActivity::class.java)
        startActivity(NearByResultsIntent)
    }

    fun buttonClicked(position:Int){
        SearchResults(citiesList.get(position))

    }
    private fun registerClickHandler() {
        val loggedInUser = sharedPreferences.getString("USER_EMAIL", "")
        if(loggedInUser == "") {
            val registerIntent = Intent(this, RegistrationActivity::class.java)
            startActivity(registerIntent)
        } else {
            Snackbar.make(binding.root, "Already Logged In.", Snackbar.LENGTH_LONG).show()
        }
    }
    private fun postrentalClickHandler() {
        val loggedInUser = sharedPreferences.getString("USER_EMAIL", "")
        if(loggedInUser != "") {
            val postrentalIntent = Intent(this, PostRentalActivity::class.java)
            startActivity(postrentalIntent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun loginClickHandler() {
        val loggedInUser = sharedPreferences.getString("USER_EMAIL", "")
        if(loggedInUser == "") {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        } else {
            Snackbar.make(binding.root, "Already Logged In.", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val loggedInUser = sharedPreferences.getString("USER_EMAIL", "")
        return when(item.itemId){

            R.id.menuMyListings -> {
                if(loggedInUser != ""){
                    val myListingIntent = Intent(this, MyListingsActivity::class.java)
                    startActivity(myListingIntent)
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                return true

            }
            R.id.menuPostRental -> {
                if(loggedInUser != ""){
                    val postRentalIntent = Intent(this, PostRentalActivity::class.java)
                    startActivity(postRentalIntent)
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                return true
            }
            R.id.menuLogout -> {
                prefEditor.remove("USER_EMAIL")
                prefEditor.remove("USER_PASSWORD")
                prefEditor.apply()
                this.firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                return  true
            }
            R.id.menuHome -> {
                return  true
            }
            R.id.menuShortList -> {
                if(loggedInUser != ""){
                    startActivity(Intent(this, MyShortlistingsActivity::class.java))
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                startActivity(Intent(this, MyShortlistingsActivity::class.java))
                return  true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
