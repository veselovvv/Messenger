package com.veselovvv.messenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var selectPhotoButton: ImageButton
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var alreadyHaveAccountTextView: TextView

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectPhotoButton = findViewById(R.id.selectphoto_button_register)
        usernameEditText = findViewById(R.id.username_edittext_register)
        emailEditText = findViewById(R.id.email_edittext_register)
        passwordEditText = findViewById(R.id.password_edittext_register)
        registerButton = findViewById(R.id.register_button_register)
        alreadyHaveAccountTextView = findViewById(R.id.already_have_account_text_view)

        registerButton.setOnClickListener {
            performRegister()
        }

        alreadyHaveAccountTextView.setOnClickListener {
            // Launch LoginActivity:
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(bitmap)

            selectPhotoButton.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in text fields!", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Authentication to create a user with email and password:
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // If successful:
                Log.d("Main", "Successfully created user with uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {

            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, usernameEditText.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {

            }
    }
}

class User(val uid: String, val username: String, val profileImageUrl: String)