package com.veselovvv.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.last_message_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class LastMessagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    val adapter = GroupAdapter<GroupieViewHolder>()
    val lastMessagesMap = HashMap<String, ChatMessage>()

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_last_messages)

        recyclerView = findViewById(R.id.recyclerview_last_messages)
        recyclerView.adapter = adapter

        listenForLastMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
    }

    private fun listenForLastMessages() {
        val fromId = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase
            .getInstance("https://messenger-79c50-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("/last-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                lastMessagesMap[snapshot.key!!] = chatMessage

                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                lastMessagesMap[snapshot.key!!] = chatMessage

                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()

        lastMessagesMap.values.forEach {
            adapter.add(LastMessageRow(it))
        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase
            .getInstance("https://messenger-79c50-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid

        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    class LastMessageRow(val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.message_textview_last_message.text = chatMessage.text
        }

        override fun getLayout(): Int {
            return R.layout.last_message_row
        }
    }
}
