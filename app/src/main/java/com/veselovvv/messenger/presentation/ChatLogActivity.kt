package com.veselovvv.messenger.presentation

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
import com.veselovvv.messenger.R
import com.veselovvv.messenger.models.ChatMessage
import com.veselovvv.messenger.models.User
import com.veselovvv.messenger.views.ChatFromItem
import com.veselovvv.messenger.views.ChatToItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

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
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase
            .getInstance("YourDatabaseLink")
            .getReference("/user-messages/$fromId/$toId")

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

                // Scroll to the last message:
                recyclerView.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    private fun performSendMessage() {
        val text = chatEditText.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        if (fromId == null || toId == null) return

        val ref = FirebaseDatabase
            .getInstance("YourDatabaseLink")
            .getReference("/user-messages/$fromId/$toId")
            .push()

        val toRef = FirebaseDatabase
            .getInstance("YourDatabaseLink")
            .getReference("/user-messages/$toId/$fromId")
            .push()

        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis()/1000)

        ref.setValue(chatMessage)
            .addOnSuccessListener {
                // Clear chatEditText when message is sent:
                chatEditText.text.clear()

                // Scroll to the last message:
                recyclerView.scrollToPosition(adapter.itemCount - 1)
            }

        toRef.setValue(chatMessage)

        val lastMessageRef = FirebaseDatabase
            .getInstance("YourDatabaseLink")
            .getReference("/last-messages/$fromId/$toId")
        lastMessageRef.setValue(chatMessage)

        val lastMessageToRef = FirebaseDatabase
            .getInstance("YourDatabaseLink")
            .getReference("/last-messages/$toId/$fromId")
        lastMessageToRef.setValue(chatMessage)
    }
}
