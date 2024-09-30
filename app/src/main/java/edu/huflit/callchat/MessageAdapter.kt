package edu.huflit.callchat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageAdapter (val context:Context,val messageList:ArrayList<Message>):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val ITEM_REVEIVE=1
    val ITEM_SEND=2
    class sendViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val sentMessage=itemView.findViewById<TextView>(R.id.et_Sendtext_chat)
    }
    class recevieViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val recevieMessage=itemView.findViewById<TextView>(R.id.et_receiveText_chat)
        val profileImage =itemView.findViewById<CircleImageView>(R.id.civ_profile_chat)
        val recevieTime=itemView.findViewById<TextView>(R.id.tv_time_recevie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==1){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.receive_chat, parent, false)
            return recevieViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.send_chat, parent, false)
            return sendViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage=messageList[position]
        if(FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SEND
        }else{
            return ITEM_REVEIVE
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        if (holder.javaClass == sendViewHolder::class.java) {
            val viewHolder = holder as sendViewHolder
            holder.sentMessage.text = currentMessage.text

        } else {
            val viewHolder = holder as recevieViewHolder
            holder.recevieMessage.text = currentMessage.text

            val senderId = currentMessage.senderId

            // Query Firebase to get User details based on senderId
            val databaseReference =
                senderId?.let {
                    FirebaseDatabase.getInstance().reference.child("Account/User").child(
                        it
                    )
                }
            if (databaseReference != null) {
                databaseReference.get().addOnSuccessListener { dataSnapshot ->
                    val user = dataSnapshot.getValue(User::class.java)

                    user?.let {
                        // Load profile image using Glide
                        Glide.with(holder.itemView.context)
                            .load(user.profileImage) // Load the profile image URL
                            .placeholder(R.drawable.baseline_person_24) // Placeholder image
                            .into(holder.profileImage) // Assuming holder has an ImageView named profileImage
                    }
                }.addOnFailureListener { exception ->
                    // Handle the error (optional)
                    Log.e("Firebase", "Error fetching user data", exception)
                }
            }
            // để hiện time
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val recivetime: LocalDateTime = LocalDateTime.parse(currentMessage.time, formatter)

            val duration = Duration.between(recivetime, LocalDateTime.now())

            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60

            val minusPassText = when {
                hours > 0 -> "$hours hours ago"
                minutes > 0 -> "$minutes minutes ago"
                else -> "Just now"
            }
            holder.recevieTime.text=minusPassText


        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

}