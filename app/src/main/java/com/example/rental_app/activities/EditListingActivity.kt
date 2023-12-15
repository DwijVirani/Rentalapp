package com.example.rental_app.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.rental_app.MainActivity
import com.example.rental_app.R
import com.example.rental_app.databinding.ActivityEditListingBinding
import com.example.rental_app.models.Listings
import com.example.rental_app.models.User
import com.example.rental_app.repositories.PropertiesRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.Locale

class EditListingActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityEditListingBinding
    private var rowPosition:Int = -1
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var propertyTypeSelected: String
    private var propertyId: String = ""
    private lateinit var propertiesRepository: PropertiesRepository
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private lateinit var firebaseAuth: FirebaseAuth

    private val APP_PERMISSIONS_LIST = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val multiplePermissionsResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
            resultsList ->
        var allPermissionsGrantedTracker = true

        for (item in resultsList.entries) {
            if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                allPermissionsGrantedTracker = false
            }
        }

        if (allPermissionsGrantedTracker == true) {
            Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_LONG).show()
            getDeviceLocation()

        } else {
            Snackbar.make(binding.root, "Some permissions NOT granted", Snackbar.LENGTH_LONG).show()
            handlePermissionDenied()
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location === null) {
                    Log.d(TAG, "Location is null")
                    return@addOnSuccessListener
                }
                val message = "The device is located at: ${location.latitude}, ${location.longitude}"
                Log.d(TAG, message)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
    }

    private fun handlePermissionDenied() {
        val errorMsg = "Sorry, you need to give us permissions before we can get your location. Check your settings menu and update your location permissions for this app."
        Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
        binding.btnGetLocation.isEnabled = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "4Rent.ca"

        this.firebaseAuth = FirebaseAuth.getInstance()

        propertiesRepository = PropertiesRepository(applicationContext)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)

        val propertyTypes = mutableListOf<String>("Property Type","Apartment", "Basement", "Condo", "House", "Multi Family House", "Townhouse")

        val spinner: Spinner = findViewById(R.id.etPropertyType)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, propertyTypes)

        spinner.adapter = arrayAdapter
        this.binding.etPropertyType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position > 0) {
                    propertyTypeSelected = propertyTypes.get(position)
                } else {
                    Snackbar.make(binding.root, "Please select property type", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        if (intent != null) {
            this.rowPosition = intent.getIntExtra("LISTING_POSITION", -1)
            val propertyId = intent.getStringExtra("PROPERTY_ID")
            if(!propertyId.isNullOrEmpty()) {
                this.propertyId = propertyId
            }
            lifecycleScope.launch {
                val propertyData = propertiesRepository.getPropertyById(propertyId)
                val spinnerPosition = arrayAdapter.getPosition(propertyData?.propertyType)
                spinner.setSelection(spinnerPosition)
                binding.etAddress.setText(propertyData?.address)
                binding.etBuildingName.setText(propertyData?.buildingName)
                binding.etProvince.setText(propertyData?.province)
                binding.etCity.setText(propertyData?.city)
                binding.etPostalCode.setText(propertyData?.postalCode)
                binding.etBedrooms.setText(propertyData?.bedrooms.toString())
                binding.etKitchen.setText(propertyData?.kitchen.toString())
                binding.etBathrooms.setText(propertyData?.bathroom.toString())
                binding.etDescription.setText(propertyData?.description)
                binding.etRent.setText(propertyData?.rent.toString())
                binding.etImage.setText(propertyData?.img)
                binding.etPhoneNumber.setText(propertyData?.phoneNumber)
                lat = propertyData?.coordinates!!.latitude
                lng = propertyData?.coordinates!!.longitude
            }
        }

        binding.btnEditRental.setOnClickListener(this)
        binding.btnGetLocation.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btnEditRental -> {
                this.editRental()
            }
            R.id.btnGetLocation -> {
                this.getCurrentLocation()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val bedrooms = binding.etBedrooms.text.toString()
        val kitchen = binding.etKitchen.text.toString()
        val bathrooms = binding.etBathrooms.text.toString()
        val description = binding.etDescription.text.toString()
        val address = binding.etAddress.text.toString()
        val buildingName = binding.etBuildingName.text.toString()
        val postalCode = binding.etPostalCode.text.toString()
        val province = binding.etProvince.text.toString()
        val city = binding.etCity.text.toString()
        val rent = binding.etRent.text.toString()

        if(bedrooms.isNullOrEmpty()) {
            binding.etBedrooms.error = "This field is required"
            return false
        } else if(kitchen.isNullOrEmpty()) {
            binding.etKitchen.error = "This field is required"
            return false
        } else if(bathrooms.isNullOrEmpty()) {
            binding.etBathrooms.error = "This field is required"
            return false
        } else if(description.isNullOrEmpty()) {
            binding.etDescription.error = "This field is required"
            return false
        } else if(address.isNullOrEmpty()) {
            binding.etAddress.error = "This field is required"
            return false
        } else if(buildingName.isNullOrEmpty()) {
            binding.etBuildingName.error = "This field is required"
            return false
        } else if(postalCode.isNullOrEmpty()) {
            binding.etPostalCode.error = "This field is required"
            return false
        } else if(province.isNullOrEmpty()) {
            binding.etProvince.error = "This field is required"
            return false
        } else if(city.isNullOrEmpty()) {
            binding.etCity.error = "This field is required"
            return false
        } else if(rent.isNullOrEmpty()) {
            binding.etRent.error = "This field is required"
            return false
        } else {
            return true
        }
    }

    private fun editRental() {
        val inputValidation = this.validateInputs()
        if(inputValidation) {
            val bedrooms = binding.etBedrooms.text.toString().toInt()
            val kitchen = binding.etKitchen.text.toString().toInt()
            val bathrooms = binding.etBathrooms.text.toString().toDouble()
            val description = binding.etDescription.text.toString()
            val address = binding.etAddress.text.toString()
            val isAvailable = binding.swAvailable.isChecked
            val buildingName = binding.etBuildingName.text.toString()
            val postalCode = binding.etPostalCode.text.toString()
            val province = binding.etProvince.text.toString()
            val city = binding.etCity.text.toString().uppercase()
            val rent = binding.etRent.text.toString().toInt()
            val img = binding.etImage.text.toString()
            val phoneNumber = binding.etPhoneNumber.text.toString()

            val loggedInUserEmail = sharedPreferences.getString("USER_EMAIL", null)

            val streetAddress = "$address, $city, $province $postalCode"
            getCoordinatesFromAddress(streetAddress)

            if(loggedInUserEmail != null) {
                lifecycleScope.launch {
                    val propertyData = propertiesRepository.getPropertyById(propertyId)
                    val listing = Listings(propertyData!!.id, loggedInUserEmail, phoneNumber, propertyTypeSelected, bedrooms, kitchen, bathrooms, description, address, isAvailable, buildingName, postalCode, province, city, img, rent, GeoPoint(lat, lng))

                    propertiesRepository.updateProperty(listing)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
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

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())
                if (location == null) {
                    Log.d(TAG, "Location is null")
                    return@addOnSuccessListener
                }
                val message = "The device is located at: ${location.latitude}, ${location.longitude}"
                try {
                    val searchResults: MutableList<Address>? =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (searchResults == null) {
                        Log.d(TAG, "ERROR: When retrieving results")
                    }
                    else if (searchResults.size == 0){
                        Log.d(TAG, "ERROR: No result found")
                    }
                    else{
                        Log.d(TAG, "getCurrentLocation: ****************************************************")
                        val listingAddress = "${searchResults[0].featureName} ${searchResults[0].thoroughfare}"
                        val addressEditText = findViewById<EditText>(R.id.etAddress)
                        val cityEditText = findViewById<EditText>(R.id.etCity)
                        val provinceEditText = findViewById<EditText>(R.id.etProvince)
                        val postalCodeEditText = findViewById<EditText>(R.id.etPostalCode)

                        addressEditText.setText(listingAddress)
                        cityEditText.setText(searchResults[0].locality)
                        provinceEditText.setText(searchResults[0].adminArea)
                        postalCodeEditText.setText(searchResults[0].postalCode)

                        lat = searchResults[0].latitude
                        lng = searchResults[0].longitude
                    }
                } catch (exception:Exception) {
                    Log.d(TAG, "Exception occurred while getting matching address")
                    Log.d(TAG, exception.toString())
                }

                Log.d(TAG, message)
            }
            .addOnFailureListener{
                Log.d(TAG, "getCurrentLocation: addOnFailureListener: $it")
            }

    }

    private fun getCoordinatesFromAddress(streetAddress: String) {
        val geocoder:Geocoder = Geocoder(applicationContext, Locale.getDefault())
        try {
            val searchResults:MutableList<Address>? = geocoder.getFromLocationName(streetAddress, 1)
            if (searchResults == null) {
                Log.e(TAG, "searchResults variable is null")
                return
            }
            if (searchResults.size == 0) {
                Log.e(TAG, "getCoordinatesFromAddress: Search results are empty.", )
            } else {
                val foundLocation:Address = searchResults[0]
                lat = foundLocation.latitude
                lng = foundLocation.longitude
            }


        } catch(ex:Exception) {
            Log.e(TAG, "Error encountered while getting coordinate location.")
            Log.e(TAG, ex.toString())
        }
    }
}