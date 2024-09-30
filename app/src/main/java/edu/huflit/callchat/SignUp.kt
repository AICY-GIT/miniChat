package edu.huflit.callchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SignUp : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var btReg: Button
    private lateinit var tvBack: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etEmail = findViewById(R.id.emailEditText)
        etPassword = findViewById(R.id.passwordEditText)
        etName=findViewById(R.id.nameEditText)
        btReg = findViewById(R.id.regButton)
        tvBack = findViewById(R.id.backText)
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Account/User")

        btReg.setOnClickListener{

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name=etName.text.toString().trim()
            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userID = mAuth.currentUser?.uid
                    val currentUser = databaseReference.child(userID!!)
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "uid" to userID
                    )
                    currentUser.setValue(user).addOnCompleteListener {
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }

        }
        tvBack.setOnClickListener{
            val intent=Intent(this,Login::class.java)
            startActivity(intent)
            finish()
        }

    }
}