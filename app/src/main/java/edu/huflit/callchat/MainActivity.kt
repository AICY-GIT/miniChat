package edu.huflit.callchat

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserAdapter
    private lateinit var mAuth:FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var checkMarkAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkMarkAnimation = findViewById(R.id.check_mark_animation)
        userRecyclerView=findViewById(R.id.rv_userlist_home)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Account/User")
        userList=ArrayList()
        userAdapter= UserAdapter(this,userList)
        userRecyclerView.adapter=userAdapter
        fetchDataFromFirebase()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout){
            mAuth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }else if(item.itemId==R.id.setAvt){
            pickImage()
        }else{
            return true
        }
        return true
    }

    val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val fileUri = data?.data!!

                sendToFirebase(fileUri)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun sendToFirebase(fileUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Create a reference to the user's profile image in Firebase Storage
            val fileRef = storageReference.child("Account/User/${user.uid}/profileImage.jpg")

            // Upload the file
            fileRef.putFile(fileUri)
                .addOnSuccessListener {
                    // Get the download URL once the image has been uploaded
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        // Save the download URL in the user's profile in Realtime Database
                        saveImageUrlToDatabase(uri.toString())
                        Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No signed-in user", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // Reference to the user's node in the Realtime Database
            val userRef = FirebaseDatabase.getInstance().reference.child("Account/User").child(user.uid)

            // Update the user's profileImage field with the image URL
            val updates = mapOf("profileImage" to imageUrl)

            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    showCheckMark()
                    Toast.makeText(this, "Profile image URL saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun pickImage() {
        ImagePicker.with(this)
            .compress(1024)          // Compress image (Optional)
            .maxResultSize(1080, 1080) // Image resolution limit (Optional)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }
    private fun showCheckMark() {
        checkMarkAnimation.visibility = View.VISIBLE
        checkMarkAnimation.playAnimation() // Start playing the animation
        checkMarkAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                checkMarkAnimation.visibility = View.GONE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun fetchDataFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userList.clear()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let { userList.add(it) }
                    }
                    userAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {

                Log.w("MainActivity", "loadPost:onCancelled", error.toException())
            }
        })
    }


}