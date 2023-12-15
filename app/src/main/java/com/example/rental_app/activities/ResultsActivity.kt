package com.example.rental_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.rental_app.MainActivity
import com.example.rental_app.MapboxUtils
import com.example.rental_app.R
import com.example.rental_app.adapters.ResultsAdaptor
import com.example.rental_app.databinding.ActivityResultsBinding
import com.example.rental_app.models.Listings
import com.example.rental_app.models.User
import com.example.rental_app.repositories.PropertiesRepository
import com.example.rental_app.repositories.UserRepository
import com.google.android.material.snackbar.Snackbar
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

class ResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultsBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var propertiesRepository: PropertiesRepository
    private lateinit var userRepository: UserRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var filteredList:MutableList<Listings>
    private lateinit var loggedInUserEmail:String
    private lateinit var adapter: ResultsAdaptor
    private val TAG = javaClass.canonicalName
    private lateinit var originalList: MutableList<Listings>
    private var currentView = "listview"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(this.binding.root)
        this.sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        prefEditor = sharedPreferences.edit()

        binding.rv.isVisible = true
        binding.mapView.isVisible = false

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        this.firebaseAuth = FirebaseAuth.getInstance()

        propertiesRepository = PropertiesRepository(applicationContext)
        userRepository = UserRepository(applicationContext)

        loggedInUserEmail = sharedPreferences.getString("USER_EMAIL", "").toString()

        filteredList = mutableListOf()
        originalList = mutableListOf()

        var receivedData = intent.getStringExtra("SEARCH_VAL").toString()
        receivedData = receivedData.trim().uppercase()

        val propertyTypes = mutableListOf<String>("Property Type","Apartment", "Basement", "Condo", "House", "Multi Family House", "Townhouse")

        val spinner: Spinner = findViewById(R.id.etPropertyType)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, propertyTypes)

        spinner.adapter = arrayAdapter
        this.binding.etPropertyType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position > 0) {
                    val propertyTypeFilteredList =  originalList.filter { it.propertyType == propertyTypes[position] }
                    Log.d(TAG, "onItemSelected: propertyTypeFilteredList: $propertyTypeFilteredList")
                    filteredList.clear()
                    filteredList.addAll(propertyTypeFilteredList)
                    adapter.notifyDataSetChanged()
                }
                else if(position == 0){
                    filteredList.clear()
                    filteredList.addAll(originalList)
                    adapter.notifyDataSetChanged()

                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.searchedValue.text = "${receivedData} :"

        Log.d("please", "onCreate: $receivedData")


        adapter = ResultsAdaptor(
            filteredList,
            {pos -> rowClicked(pos) },
            {pos -> callButtonClicked(pos)},
            {pos -> enquiryButtonClicked(pos)}
        )
        binding.rv.adapter = adapter
        binding.rv.layoutManager = GridLayoutManager(this, 1)


        lifecycleScope.launch{
            propertiesRepository.filterByCity(receivedData)
        }
        this.propertiesRepository.filteredResultsByName?.observe(this) { receivedData ->
            if (receivedData.isNotEmpty()) {
                filteredList.clear()
                filteredList.addAll(receivedData)
                originalList.addAll(receivedData)
                Log.d("getUserListings1", "onCreate: ${filteredList.get(0).city}")
                adapter.notifyDataSetChanged()

                val initialCameraOptions = CameraOptions.Builder()
                    .center(Point.fromLngLat(filteredList[0].coordinates.longitude, filteredList[0].coordinates.latitude))
                    .pitch(0.0)
                    .zoom(8.0)
                    .bearing(0.0)
                    .build()
                binding.mapView.mapboxMap.setCamera(initialCameraOptions)
                addManyAnnotations(filteredList)
            } else {
                Log.d("resultactivity", "onStart: No data received from observer")
                binding.empty.isVisible = true
                Handler().postDelayed({
                    finish()
                }, 2000)
            }
        }

        binding.listView.setOnClickListener {
            currentView = "listview"
            binding.mapView.isVisible = false
            binding.rv.isVisible = true
            binding.etPropertyType.isVisible = true
        }
        binding.mapViewBtn.setOnClickListener {
            currentView = "mapview"
            binding.mapView.isVisible = true
            binding.rv.isVisible = false
            binding.etPropertyType.isVisible = false
        }

    }


    fun rowClicked(position:Int){
        if(loggedInUserEmail != "") {
            val descriptionIntent = Intent(this, ResultDescriptionActivity::class.java)
            descriptionIntent.putExtra("PROPERTY_ID", filteredList[position].id)
            descriptionIntent.putExtra("SOURCE", "ResultActivity")
            startActivity(descriptionIntent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
    fun callButtonClicked(position: Int){
        val ph_no = filteredList[position].phoneNumber.toString()
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$ph_no")
        }

        if (callIntent.resolveActivity(packageManager) != null){
            startActivity(callIntent)
        }
    }
    fun enquiryButtonClicked(position: Int){
        val inquiryIntent = Intent(this, InquiryActivity::class.java)
        inquiryIntent.putExtra("PROPERTY_ID", filteredList[position].id)
        startActivity(inquiryIntent)
    }

    private fun IntentByCoordinates(clickedCoordinate: Point) {
        for (property in filteredList) {

            val geoPoint = property.coordinates

            geoPoint?.let {
                val point = Point.fromLngLat(geoPoint.longitude, geoPoint.latitude)
                if (point == clickedCoordinate) {
                    if(loggedInUserEmail != "") {
                        val descriptionIntent = Intent(this, ResultDescriptionActivity::class.java)
                        descriptionIntent.putExtra("PROPERTY_ID", property.id)
                        descriptionIntent.putExtra("SOURCE", "ResultActivity")
                        startActivity(descriptionIntent)
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
            }
        }

    }

    private fun addManyAnnotations(ListingsList:MutableList<Listings>, @DrawableRes drawableImageResourceId: Int = R.drawable.red_marker) {

        val icon = MapboxUtils.bitmapFromDrawableRes(applicationContext, drawableImageResourceId)

        if (icon == null) {
            Log.d("addMany", "ERROR: Unable to convert provided image into the correct format.")
            return
        }

        val annotationApi = binding.mapView.annotations
        val pointAnnotationManager =
            annotationApi?.createPointAnnotationManager(
                AnnotationConfig(
                    annotationSourceOptions = AnnotationSourceOptions(maxZoom = 16)
                )
            )

        // loop through our list of coordinates & add them to the map
        val pointAnnotationOptionsList: MutableList<PointAnnotationOptions> = ArrayList()


        for (property in ListingsList){
            pointAnnotationOptionsList.add(
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(property.coordinates.longitude,property.coordinates.latitude))
                    .withIconImage(icon)
            )
        }

        Log.d(TAG, "addManyAnnotations: $pointAnnotationManager")

        pointAnnotationManager?.create(pointAnnotationOptionsList)

        pointAnnotationManager?.addClickListener { pointAnnotation ->
            val clickedCoordinate = pointAnnotation.geometry

            IntentByCoordinates(clickedCoordinate)

            return@addClickListener true
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
}