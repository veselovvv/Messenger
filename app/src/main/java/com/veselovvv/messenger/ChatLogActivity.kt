package com.veselovvv.messenger

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sendButton: Button
    private lateinit var chatEditText: EditText

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView = findViewById(R.id.recyclerview_chat_log)
        sendButton = findViewById(R.id.send_button_chat_log)
        chatEditText = findViewById(R.id.edittext_chat_log)

        recyclerView.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        sendButton.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val ref = FirebaseDatabase
            .getInstance("https://messenger-79c50-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("/messages")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LastMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        val text = chatEditText.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        if (fromId == null || toId == null) return

        val ref = FirebaseDatabase
            .getInstance("https://messenger-79c50-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("/messages")
            .push()

        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis()/1000)

        ref.setValue(chatMessage)
            .addOnSuccessListener {

            }
    }
}

class ChatFromItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text = text

        // Load user image into the CircleImageView:
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_from_row

        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text = text

        // Load user image into the CircleImageView:
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_to_row

        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
