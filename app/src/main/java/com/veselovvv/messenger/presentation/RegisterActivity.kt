 package com.veselovvv.messenger.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.veselovvv.messenger.R
import com.veselovvv.messenger.models.User
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

 class RegisterActivity : AppCompatActivity() {

    private lateinit var selectPhotoButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var selectPhotoCircleImageView: CircleImageView
    private lateinit var alreadyHaveAccountTextView: TextView

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        selectPhotoButton = findViewById(R.id.selectphoto_button_register)
        usernameEditText = findViewById(R.id.username_edittext_register)
        emailEditText = findViewById(R.id.email_edittext_register)
        passwordEditText = findViewById(R.id.password_edittext_register)
        registerButton = findViewById(R.id.register_button_register)
        selectPhotoCircleImageView = findViewById(R.id.selectphoto_imageview_register)
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

            selectPhotoCircleImageView.setImageBitmap(bitmap)
            selectPhotoButton.alpha = 0f
        }
    }

    private fun performRegister() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.please_enter_text, Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Authentication to create a user with email and password:
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // If successful:
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "${R.string.register_failed}: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
            .addOnFailureListener {}
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase
            .getInstance("YourDatabaseLink")
            .getReference("/users/$uid")

        val user = User(uid, usernameEditText.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, LastMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }
}
