package com.example.rental_app.models
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import java.util.UUID

class Listings(
    var id : String = UUID.randomUUID().toString(),
    var owner: String = "",
    var phoneNumber: String = "" ,
    var propertyType:String = "",
    var bedrooms:Int = 0,
    var kitchen:Int = 0,
    var bathroom: Double = 0.0,
    var description: String = "",
    var address:String = "",
    var isAvailable:Boolean = false,
    var buildingName:String = "",
    var postalCode:String = "",
    var province:String = "",
    var city:String = "",
    var img:String = "",
    var rent:Int = 0,
    var coordinates: GeoPoint = GeoPoint(0.0,0.0)
) {
}