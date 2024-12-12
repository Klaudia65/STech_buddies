package com.example.stech_buddies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)

        fun bind(message: Message) {
            senderTextView.text = "From: ${message.sender}"
            contentTextView.text = message.content
            timestampTextView.text = formatTimestamp(message.timestamp)
        }

        private fun formatTimestamp(timestamp: Timestamp?): String {
            return timestamp?.toDate()?.let {
                val sdf = SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault())
                sdf.format(it)
            } ?: ""
        }
    }
}
