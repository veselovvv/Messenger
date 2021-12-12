package com.veselovvv.messenger.views

import com.squareup.picasso.Picasso
import com.veselovvv.messenger.R
import com.veselovvv.messenger.models.User
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_to_row.view.*

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