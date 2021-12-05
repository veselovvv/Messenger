package com.veselovvv.messenger

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var backToRegisterTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.email_edittext_login)
        passwordEditText = findViewById(R.id.password_edittext_login)
        loginButton = findViewById(R.id.login_button_login)
        backToRegisterTextView = findViewById(R.id.back_to_register_textview)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
               // .addOnCompleteListener()
        }

        backToRegisterTextView.setOnClickListener {
            finish()
        }
    }
}