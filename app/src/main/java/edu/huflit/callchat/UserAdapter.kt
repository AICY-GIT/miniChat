package edu.huflit.callchat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(val context: Context, val userList:ArrayList<User>):RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.name

        // Load profile image using Glide (or Picasso)
        Glide.with(holder.itemView.context)
            .load(user.profileImage)
            .placeholder(R.drawable.baseline_person_24) // Placeholder image
            .into(holder.profileImage)


        holder.itemView.setOnClickListener{
            val intent=Intent(context,ActivityChat::class.java)
            intent.putExtra("name",user.name)
            intent.putExtra("uid",user.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: CircleImageView = view.findViewById(R.id.civ_profile_chathome)
        val userName: TextView = view.findViewById(R.id.textView2)
    }

}
