package com.example.rental_app.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import com.example.rental_app.MainActivity
import com.example.rental_app.R
import com.example.rental_app.databinding.ActivityLoginBinding
import com.example.rental_app.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = this.javaClass.canonicalName
    private lateinit var prefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MY_APP_PREFS", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        this.firebaseAuth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.loginButton -> {
                    Log.d(TAG, "onClick: Sign In Button Clicked")
                    this.validateData()
                }
            }
        }
    }

    private fun validateData() {
        var validData = true
        var email = ""
        var password = ""
        if ( binding.username.text.toString().isEmpty()) {
            binding.username.error = "Email Cannot be Empty"
            validData = false
        } else {
            email = binding.username.text.toString()
        }
        if (binding.password.text.toString().isEmpty()) {
            binding.password.error = "Password Cannot be Empty"
            validData = false
        } else {
            password = binding.password.text.toString()
        }
        if (validData) {
            signIn(email, password)
        } else {
            Toast.makeText(this, "Please provide correct inputs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn(email: String, password: String){
        this.firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){task ->
            if(task.isSuccessful) {
                Log.d(TAG, "signIn: Login Successful: ${task.result.user?.uid}")
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                saveToPrefs(email, password)
                goToMain()
            } else {
                Log.d(TAG, "signIn: Login Failed: ${task.exception}")
                Toast.makeText(this, "Authentication Failed. Check your credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToPrefs(email: String, password: String) {
//        val prefs = applicationContext.getSharedPreferences(packageName, MODE_PRIVATE)
//        prefs.edit().putString("USER_EMAIL", email).apply()
//        prefs.edit().putString("USER_PASSWORD", password).apply()
        this.prefEditor.putString("USER_EMAIL", email)
        this.prefEditor.putString("USER_PASSWORD", password)
        this.prefEditor.apply()
    }

    private fun goToMain() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
    }
}
