package com.example.stech_buddies

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stech_buddies.databinding.ActivityMessagingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessagingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatroomId: String
    private lateinit var adapter: MessagesAdapter
    private val messages = mutableListOf<Message>()
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatroomId = intent.getStringExtra("groupId") ?: ""
        val groupName = intent.getStringExtra("groupName") ?: "Chatroom"
        title = groupName

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchUsername() // Fetch username before allowing message sending
        fetchMessages()

        binding.buttonSendMsg.setOnClickListener {
            val messageContent = binding.msgToSend.text.toString().trim()
            if (messageContent.isNotEmpty() && username.isNotEmpty()) {
                sendMessage(messageContent)
                binding.msgToSend.text.clear()
            } else {
                Toast.makeText(this, "Message content or username missing!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = MessagesAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun fetchMessages() {
        db.collection("chatrooms").document(chatroomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Failed to load messages.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                messages.clear()
                for (doc in snapshots!!) {
                    val message = doc.toObject(Message::class.java)
                    messages.add(message)
                }
                adapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage(content: String) {
        val message = hashMapOf(
            "sender" to username,
            "type" to "text",
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("chatrooms").document(chatroomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUsername() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        username = document.getString("username") ?: "Anonymous"
                    } else {
                        username = "Anonymous"
                        Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    username = "Anonymous"
                    Toast.makeText(this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show()
                }
        } else {
            username = "Anonymous"
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }
}
