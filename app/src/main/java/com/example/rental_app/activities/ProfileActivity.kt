package com.example.rental_app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rental_app.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userEmail = intent.getStringExtra("USER_EMAIL")
        binding.tvUserEmail.text = userEmail ?: "No Email Provided"
    }
}