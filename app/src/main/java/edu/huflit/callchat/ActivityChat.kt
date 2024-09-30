package edu.huflit.callchat

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActivityChat : AppCompatActivity() {


    private lateinit var chatRecycleView:RecyclerView
    private lateinit var messbox:EditText
    private lateinit var sendButton:CircleImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList:ArrayList<Message>
    private lateinit var databaseReference: DatabaseReference
    var receiverRoom:String?=null
    var senderRoom:String?=null
    private lateinit var userMap: MutableMap<String, User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatRecycleView = findViewById(R.id.rv_chat_chat)
        messbox = findViewById(R.id.et_chat_box_chat)
        sendButton = findViewById(R.id.civ_profile_chathome)
        databaseReference = FirebaseDatabase.getInstance().reference
        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        supportActionBar?.title = name
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
        chatRecycleView.layoutManager = LinearLayoutManager(this)
        chatRecycleView.adapter = messageAdapter
        // Adding data for RecyclerView
        updateRecyclerView()

        // Adding chat data to Firebase
        sendButton.setOnClickListener {
            val message = messbox.text.toString()
            val messageObject = Message(
                message,
                senderUid,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // Convert LocalDateTime to String
            )
            databaseReference.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    databaseReference.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messbox.setText("")
        }
    }
    private fun updateRecyclerView() {
        databaseReference.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        message?.let { messageList.add(it) }
                    }
                    messageAdapter.notifyDataSetChanged()
                    chatRecycleView.scrollToPosition(messageList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error loading messages", error.toException())
                }
            })
    }
}