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

    // Creates new ViewHolder a message item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    // Binds message data to the ViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    // Returns total number of messages
    override fun getItemCount(): Int = messages.size

    // ViewHolder for displaying individual message details
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderTextView: TextView = itemView.findViewById(R.id.senderTextView) // Sender's name
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView) // Message timestamp
        private val contentTextView: TextView = itemView.findViewById(R.id.contentTextView) // Message content

        // Populates views with data from the message object
        fun bind(message: Message) {
            senderTextView.text = "From: ${message.sender}"
            contentTextView.text = message.content
            timestampTextView.text = formatTimestamp(message.timestamp)
        }

        // Formats timestamp into a readable string
        private fun formatTimestamp(timestamp: Timestamp?): String {
            return timestamp?.toDate()?.let {
                val sdf = SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault())
                sdf.format(it)
            } ?: ""
        }
    }
}
