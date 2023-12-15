package com.example.rental_app.models

class User(
    val id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var listings: MutableList<String> = mutableListOf(),
    var shortListing: MutableList<String> = mutableListOf(),
    var isLoggedIn: Boolean = false,

) {

//    fun addListing(listing: Listings) {
//        shortListing.add(listing)
//    }
//
//
//    fun removeListing(listing: Listings) {
//        shortListing.remove(listing)
//    }

    override fun toString(): String {
        return "User(firstName='$firstName', lastName='$lastName', email='$email', phoneNumber='$phoneNumber', password='$password', isLoggedIn=$isLoggedIn, listings=$shortListing)"
    }
}