package com.example.rental_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.rental_app.MainActivity
import com.example.rental_app.MapboxUtils
import com.example.rental_app.R
import com.example.rental_app.databinding.ActivityResultDescriptionBinding
import com.example.rental_app.models.Listings
import com.example.rental_app.models.User
import com.example.rental_app.repositories.PropertiesRepository
import com.example.rental_app.repositories.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.launch
import java.util.Locale

class ResultDescriptionActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityResultDescriptionBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private var loggedUserObject: User? = User("", "", "", "", "", "")
    private lateinit var propertiesRepository: PropertiesRepository
    private lateinit var userRepository: UserRepository
    private val TAG = javaClass.canonicalName
    private var firebasePropertyId: String? = ""
    private var propertyId: String? = ""
    private lateinit var phoneNumber:String
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = ActivityResultDescriptionBinding.inflate(layoutInflater)
        setContentView(this.binding.root)

        this.sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        prefEditor = sharedPreferences.edit()

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        this.firebaseAuth = FirebaseAuth.getInstance()

        propertiesRepository = PropertiesRepository(applicationContext)
        userRepository = UserRepository(applicationContext)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        propertiesRepository = PropertiesRepository(applicationContext)
        userRepository = UserRepository(applicationContext)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                var city: String = ""
                val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())
                if (location === null) {
                    Log.d("nearmeactivity", "Location is null")
                    return@addOnSuccessListener
                }
                // Output the location
                val message =
                    "The device is located at: ${location.latitude}, ${location.longitude}"
                Log.d("mayank", "onCreate:${message} ")

                addAnnotationToMap(location.latitude,location.longitude,R.drawable.mylocation)
            }

        var sourceScreen = intent.getStringExtra("SOURCE")
        if(sourceScreen == "MyListing") {
            binding.callOwner.isVisible = false
            binding.enquiry.isVisible = false
        }

        val loggedInUserEmail = sharedPreferences.getString("USER_EMAIL", "")
        if (loggedInUserEmail != "") {
            lifecycleScope.launch{
                loggedUserObject = userRepository.getLoggedInUser(loggedInUserEmail)
                Log.d(TAG, "onStart: userData: ${loggedUserObject?.listings}")
            }

        }

        val propertyId = intent.getStringExtra("PROPERTY_ID")
        this.propertyId = propertyId
        lifecycleScope.launch{
            val propertyData: Listings? = propertiesRepository.getPropertyById(propertyId)
            val firebaseListingId = propertiesRepository.getPropertyIdById(propertyId)
            firebasePropertyId = firebaseListingId


            phoneNumber = propertyData?.phoneNumber.toString()
            if (propertyData != null){
                val initialCameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(propertyData.coordinates.longitude, propertyData.coordinates.latitude))
                    .pitch(0.0)
                    .zoom(8.0)
                    .bearing(0.0)
                    .build()
                binding.mapView.mapboxMap.setCamera(initialCameraOptions)
                addAnnotationToMap(propertyData.coordinates.latitude,propertyData.coordinates.longitude,R.drawable.red_marker)
            }

            val res = resources.getIdentifier(propertyData?.img, "drawable", packageName)
            binding.listingImg.setImageResource(res)
            binding.rent.text = "$${propertyData?.rent}/-"
            binding.desc.text = propertyData?.description
            binding.address.text = propertyData?.address
            binding.specs.text = "${propertyData?.bedrooms}Bed|${propertyData?.bathroom}Bath|\n${propertyData?.kitchen}Kitchen"
            binding.cityProvince.text = "${propertyData?.city},${propertyData?.province}"
        }

        binding.shortlist.setOnClickListener {
            lifecycleScope.launch {
                propertiesRepository.updateUserFavourites(loggedInUserEmail, firebasePropertyId)
                finish()
            }
        }

        binding.mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Disable scrolling when touching the nonScrollableView
                    binding.scrollBlock.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Enable scrolling when the touch is released or canceled
                    binding.scrollBlock.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        binding.callOwner.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${phoneNumber}")
            }

            if (callIntent.resolveActivity(packageManager) != null){
                startActivity(callIntent)
            }
        }

        binding.enquiry.setOnClickListener {
            val inquiryIntent = Intent(this, InquiryActivity::class.java)
            inquiryIntent.putExtra("PROPERTY_ID", propertyId)
            startActivity(inquiryIntent)
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
                startActivity(Intent(this, MainActivity::class.java))
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

    private fun addAnnotationToMap(lat:Double, lng:Double, @DrawableRes drawableImageResourceId: Int = R.drawable.red_marker) {
        Log.d("addOne", "Attempting to add annotation to map")

        val icon = MapboxUtils.bitmapFromDrawableRes(applicationContext, drawableImageResourceId)

        // error handling code: sometimes, the person may provide an image that cannot be
        // properly converted to a map marker
        if (icon == null) {
            Log.d("addOne", "ERROR: Unable to convert provided image into the correct format.")
            return
        }


        // code sets up the map so you can add markers
        val annotationApi = binding.mapView?.annotations
        val pointAnnotationManager =
            annotationApi?.createPointAnnotationManager(
                AnnotationConfig(
                    annotationSourceOptions = AnnotationSourceOptions(maxZoom = 16)
                )
            )

        // Create a marker & configure the options for that marker
        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(lng, lat))
            .withIconImage(icon)

        // Add the resulting pointAnnotation to the map.
        pointAnnotationManager?.create(pointAnnotationOptions)


    }
}