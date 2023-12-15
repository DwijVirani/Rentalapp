package com.example.rental_app.repositories

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.rental_app.models.Listings
import com.example.rental_app.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.mapbox.geojson.Point
import kotlinx.coroutines.tasks.await

class PropertiesRepository(private val context: Context) {
    private var sharedPrefs: SharedPreferences = context.getSharedPreferences("MY_APP_PREFS", Context.MODE_PRIVATE)
    private var loggedInUserEmail = ""

    private val COLLECTION_USERS = "Users"
    private val COLLECTION_PROPERTIES = "Listings"
    private val FIELD_ID = "id"
    private val FIELD_USER = "user"
    private val FIELD_PROPERTY_TYPE = "propertyType"
    private val FIELD_BEDROOMS = "bedrooms"
    private val FIELD_KITCHEN = "kitchen"
    private val FIELD_BATHROOMS = "bathroom"
    private val FIELD_DESCRIPTION = "description"
    private val FIELD_ADDRESS = "address"
    private val FIELD_IS_AVAILABLE = "isAvailable"
    private val FIELD_BUILDING_NAME = "buildingName"
    private val FIELD_POSTAL_CODE = "postalCode"
    private val FIELD_PROVINCE = "province"
    private val FIELD_CITY = "city"
    private val FIELD_IMG = "img"
    private val FIELD_RENT = "rent"
    private val FIELD_PHONE_NUMBER = "phoneNumber"
    private val FIELD_COORDINATES = "coordinates"

    var allListings: MutableLiveData<List<Listings>> = MutableLiveData<List<Listings>>()
    var shortListings: MutableLiveData<List<Listings>> = MutableLiveData<List<Listings>>()

    var filteredResultsByName: MutableLiveData<List<Listings>> = MutableLiveData<List<Listings>>()

    var filteredResultByLocation: MutableLiveData<List<Listings>> = MutableLiveData<List<Listings>>()

    init {
        if(sharedPrefs.contains("USER_EMAIL")) {
            loggedInUserEmail = sharedPrefs.getString("USER_EMAIL", "NA").toString()
        }
    }
    private val TAG = this.toString()
    private val db = Firebase.firestore

    fun addPropertyToDB(newProperty: Listings) {
        if(loggedInUserEmail.isNotEmpty()) {
            try {
                val data: MutableMap<String, Any> = HashMap()

                data[FIELD_ID] = newProperty.id
                data[FIELD_USER] = newProperty.owner
                data[FIELD_PROPERTY_TYPE] = newProperty.propertyType
                data[FIELD_BEDROOMS] = newProperty.bedrooms
                data[FIELD_KITCHEN] = newProperty.kitchen
                data[FIELD_BATHROOMS] = newProperty.bathroom
                data[FIELD_DESCRIPTION] = newProperty.description
                data[FIELD_ADDRESS] = newProperty.address
                data[FIELD_IS_AVAILABLE] = newProperty.isAvailable
                data[FIELD_BUILDING_NAME] = newProperty.buildingName
                data[FIELD_POSTAL_CODE] = newProperty.postalCode
                data[FIELD_PROVINCE] = newProperty.province
                data[FIELD_CITY] = newProperty.city
                data[FIELD_IMG] = newProperty.img
                data[FIELD_RENT] = newProperty.rent
                data[FIELD_PHONE_NUMBER] = newProperty.phoneNumber
                data[FIELD_COORDINATES] = newProperty.coordinates

                db.collection(COLLECTION_PROPERTIES)
                    .add(data)
                    .addOnSuccessListener {docRef ->
                        Log.d(TAG, "addPropertyToDB: Document successfully added with ID : ${docRef}")
                        updateUserListings(loggedInUserEmail, docRef.id)
                    }
                    .addOnFailureListener { ex ->
                        Log.e(TAG, "addPropertyToDB: Exception occurred while adding a document : $ex", )
                    }
            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "addPropertyToDB: Unable to create listing : $ex", )
            }
        }
    }

    private fun updateUserListings(userEmail: String = loggedInUserEmail, listingId: String) {
        if(loggedInUserEmail.isNotEmpty()) {
            try {
                val userRef = db.collection(COLLECTION_USERS).document(userEmail)

                userRef.update("listings", FieldValue.arrayUnion(listingId))

            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "updateUserListings: Unable to create listing : $ex", )
            }
        }
    }

    fun updateUserFavourites(userEmail: String? = loggedInUserEmail, listingId: String?) {
        if(loggedInUserEmail.isNotEmpty() && !userEmail.isNullOrEmpty()) {
            try {
                val userRef = db.collection(COLLECTION_USERS).document(userEmail)

                userRef.update("shortListing", FieldValue.arrayUnion(listingId))

            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "updateUserListings: Unable to update user listing : $ex", )
            }
        }
    }

    fun removeUserFavourites(userEmail: String? = loggedInUserEmail, listingId: String?) {
        if(loggedInUserEmail.isNotEmpty() && !userEmail.isNullOrEmpty()) {
            try {
                val userRef = db.collection(COLLECTION_USERS).document(userEmail)

                userRef.update("shortListing", FieldValue.arrayRemove(listingId))

            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "updateUserListings: Unable to update user listing : $ex", )
            }
        }
    }

    fun getUserListings(listingIds: MutableList<String>?) {
        if(!listingIds.isNullOrEmpty()) {
            try {
                db.collection(COLLECTION_PROPERTIES)
                    .whereIn(FieldPath.documentId(),  listingIds)
                    .addSnapshotListener(EventListener{result, error ->
                        if(error != null) {
                            Log.e(TAG, "getUserListings: Listening to Properties collection failed due to error : $error")
                            return@EventListener
                        } else {
                            if(result != null) {
                                Log.d(TAG, "getUserListings: Number of documents retrieved : ${result.size()}")
                                val tempList: MutableList<Listings> = ArrayList()
                                for(docChanges in result.documentChanges) {

                                    val currentDocument : Listings = docChanges.document.toObject(Listings::class.java)
                                    Log.d(TAG, "getUserListings: currentDocument : $currentDocument")

                                    when(docChanges.type){
                                        DocumentChange.Type.ADDED -> {
                                            //do necessary changes to your local list of objects
                                            tempList.add(currentDocument)
                                        }
                                        DocumentChange.Type.MODIFIED -> {

                                        }
                                        DocumentChange.Type.REMOVED -> {

                                        }
                                    }
                                }
                                Log.d(TAG, "getUserListings: tempList : $tempList")
                                allListings.postValue(tempList)
                            }
                        }
                    })
            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "getUserListings: Unable to create listing : $ex", )
            }
        }
    }

    fun getUserFavourites(listingIds: MutableList<String>?)  {
        if(!listingIds.isNullOrEmpty()) {
            try {
                db.collection(COLLECTION_PROPERTIES)
                    .whereIn(FieldPath.documentId(),  listingIds)
                    .addSnapshotListener(EventListener{result, error ->
                        if(error != null) {
                            Log.e(TAG, "getUserListings: Listening to Properties collection failed due to error : $error")
                            return@EventListener
                        } else {
                            if(result != null) {
                                Log.d(TAG, "getUserListings: Number of documents retrieved : ${result.size()}")
                                val tempList: MutableList<Listings> = ArrayList()
                                for(docChanges in result.documentChanges) {

                                    val currentDocument : Listings = docChanges.document.toObject(Listings::class.java)
                                    Log.d(TAG, "getUserListings: currentDocument : $currentDocument")

                                    when(docChanges.type){
                                        DocumentChange.Type.ADDED -> {
                                            //do necessary changes to your local list of objects
                                            tempList.add(currentDocument)
                                        }
                                        DocumentChange.Type.MODIFIED -> {

                                        }
                                        DocumentChange.Type.REMOVED -> {

                                        }
                                    }
                                }
                                Log.d(TAG, "getUserListings: tempList : $tempList")
                                shortListings.postValue(tempList)
                            }
                        }
                    })
            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "getUserListings: Unable to create listing : $ex", )
            }
        }
    }

    suspend fun updateProperty(propertyToUpdate: Listings) {
        try {
            val data: MutableMap<String, Any> = HashMap()
            data[FIELD_ID] = propertyToUpdate.id
            data[FIELD_USER] = propertyToUpdate.owner
            data[FIELD_PROPERTY_TYPE] = propertyToUpdate.propertyType
            data[FIELD_BEDROOMS] = propertyToUpdate.bedrooms
            data[FIELD_KITCHEN] = propertyToUpdate.kitchen
            data[FIELD_BATHROOMS] = propertyToUpdate.bathroom
            data[FIELD_DESCRIPTION] = propertyToUpdate.description
            data[FIELD_ADDRESS] = propertyToUpdate.address
            data[FIELD_IS_AVAILABLE] = propertyToUpdate.isAvailable
            data[FIELD_BUILDING_NAME] = propertyToUpdate.buildingName
            data[FIELD_POSTAL_CODE] = propertyToUpdate.postalCode
            data[FIELD_PROVINCE] = propertyToUpdate.province
            data[FIELD_CITY] = propertyToUpdate.city
            data[FIELD_IMG] = propertyToUpdate.img
            data[FIELD_RENT] = propertyToUpdate.rent
            data[FIELD_PHONE_NUMBER] = propertyToUpdate.phoneNumber
            data[FIELD_COORDINATES] = propertyToUpdate.coordinates

            val querySnapshot = db.collection(COLLECTION_PROPERTIES).whereEqualTo("id", propertyToUpdate.id).limit(1).get().await()
            if(!querySnapshot.isEmpty) {
                val documentId = querySnapshot.documents[0].id
                Log.d(TAG, "updateProperty: documentId: $documentId")
                db.collection(COLLECTION_PROPERTIES)
                    .document(documentId)
                    .update(data)
                    .await()
            }

        } catch (ex : java.lang.Exception) {
            Log.e(TAG, "updateUserListings: Unable to update listing : $ex", )
        }
    }

    suspend fun getPropertyById(propertyId: String?): Listings? {
        if(!propertyId.isNullOrEmpty()) {
            return try {
                val querySnapshot = db.collection(COLLECTION_PROPERTIES).whereEqualTo("id", propertyId).limit(1).get().await()
                if(!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val propertyData = documentSnapshot.toObject(Listings::class.java)
                    propertyData
                } else {
                    null
                }
            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "getPropertyById: Unable to get listing : $ex", )
                null
            }
        }
        return null
    }

    suspend fun getPropertyIdById(propertyId: String?): String? {
        if(!propertyId.isNullOrEmpty()) {
            return try {
                val querySnapshot = db.collection(COLLECTION_PROPERTIES).whereEqualTo("id", propertyId).limit(1).get().await()
                if(!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id
                    documentId
                } else {
                    null
                }
            } catch (ex : java.lang.Exception) {
                Log.e(TAG, "getPropertyById: Unable to get listing : $ex", )
                null
            }
        }
        return null
    }

    fun filterByCity(cityName:String){
        Log.d("please", "filterByCity: $cityName")
        if (!cityName.isNullOrEmpty()){
            try {
                db.collection(COLLECTION_PROPERTIES)
                    .whereEqualTo("city",cityName)
                    .whereEqualTo("isAvailable",true)
                    .addSnapshotListener(EventListener{result, error ->
                        if (error != null){
                            Log.e(TAG, "filterByCity: Listening failed:$error " )
                            return@EventListener
                        }else{
                            if (result != null){
                                Log.d(TAG, "getListingsResults: Number of documents retrieved : ${result.size()}")
                                val tempList: MutableList<Listings> = ArrayList()
                                for(docChanges in result.documentChanges) {

                                    val currentDocument: Listings =
                                        docChanges.document.toObject(Listings::class.java)
                                    Log.d(
                                        TAG,
                                        "getUserListings: currentDocument : ${currentDocument.city}"
                                    )
                                    when (docChanges.type) {
                                        DocumentChange.Type.ADDED -> {
                                            //do necessary changes to your local list of objects
                                            tempList.add(currentDocument)
                                        }

                                        DocumentChange.Type.MODIFIED -> {

                                        }

                                        DocumentChange.Type.REMOVED -> {

                                        }
                                    }
                                }
                                filteredResultsByName.postValue(tempList)
                                Log.d("getUserListings", "filterByCity: ${tempList}")
                            }
                        }
                    })
            }catch (ex: java.lang.Exception){
                Log.e(TAG, "searchResults: Unable to fetch results for the city searched", )
            }
        }
    }
    fun filterByCurrentLocation(coordinates:Point){
        if (coordinates == null){
            Log.d(TAG, "filterByCurrentLocation: no coordinates provided")
        }else{
            try {
                db.collection(COLLECTION_PROPERTIES)
                    .whereEqualTo("isAvailable",true)
                    .addSnapshotListener(EventListener{result, error->
                        if(error != null) {
                            Log.e(TAG, "getUserListings: Listening to Properties collection failed due to error : $error")
                            return@EventListener
                        }
                        else{
                            if(result != null) {
                                Log.d(TAG, "getListings: Number of documents retrieved : ${result.size()}")
                                val tempList: MutableList<Listings> = ArrayList()
                                for(docChanges in result.documentChanges) {

                                    val currentDocument : Listings = docChanges.document.toObject(Listings::class.java)
                                    Log.d(TAG, "getUserListings: currentDocument : $currentDocument")

                                    when(docChanges.type){
                                        DocumentChange.Type.ADDED -> {
                                            //do necessary changes to your local list of objects
                                            val listingPoint = Location("point1")
                                            listingPoint.latitude = currentDocument.coordinates.latitude
                                            listingPoint.longitude = currentDocument.coordinates.longitude
                                            Log.d(TAG, "filterByCurrentLocation: ${coordinates.latitude()}")
                                            val devicePoint = Location("point2")
                                            devicePoint.latitude = coordinates.latitude()
                                            devicePoint.longitude = coordinates.longitude()
                                            val distance = listingPoint.distanceTo(devicePoint)
                                            Log.d(TAG, "filterByCurrentLocation: distance :${distance}")

                                            if (distance/1000 <= 25.0){
                                                tempList.add(currentDocument)
                                            }

                                        }
                                        DocumentChange.Type.MODIFIED -> {

                                        }
                                        DocumentChange.Type.REMOVED -> {

                                        }
                                    }
                                }
                                Log.d(TAG, "getUserListings: tempList : $tempList")
                                filteredResultByLocation.postValue(tempList)
                                Log.d(TAG, "getUserListings: tempList : $filteredResultByLocation")
                            }
                        }


                    })
            }catch (er:java.lang.Exception){
                Log.e(TAG, "getListingsbylocation: Unable to create listing : $er", )
            }
        }

    }
}