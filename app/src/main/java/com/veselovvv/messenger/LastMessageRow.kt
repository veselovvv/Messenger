package com.veselovvv.messenger

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.last_message_row.view.*

class LastMessageRow(val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {

    var chatPartnerUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_textview_last_message.text = chatMessage.text

        val chatPartnerId: String

        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase
            .getInstance("https://messenger-79c50-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("/users/$chatPartnerId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.username_textview_last_message.text = chatPartnerUser?.username

                val targetImageView = viewHolder.itemView.imageview_last_message
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.last_message_row
    }
}