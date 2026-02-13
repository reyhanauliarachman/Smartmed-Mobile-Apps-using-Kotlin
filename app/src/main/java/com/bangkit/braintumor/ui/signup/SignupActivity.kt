package com.bangkit.braintumor.ui.signup

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.bangkit.braintumor.MainActivity
import com.bangkit.braintumor.R
import com.bangkit.braintumor.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val nameField = findViewById<EditText>(R.id.signup_name)
        val emailField = findViewById<EditText>(R.id.signup_email)
        val passwordField = findViewById<EditText>(R.id.signup_password)
        val signupButton = findViewById<Button>(R.id.signup_button)
        val loginRedirectText = findViewById<TextView>(R.id.loginRedirectText)

        signupButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (validateForm(name, email, password)) {
                registerUser(name, email, password)
            }
        }

        loginRedirectText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(password) || password.length < 6) {
            Toast.makeText(
                this,
                "Password harus memiliki minimal 6 karakter",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Update nama pengguna
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Pendaftaran berhasil!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Pendaftaran gagal: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
