package com.example.stech_buddies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stech_buddies.databinding.FragmentMessagingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessagingFragment : Fragment() {

    private lateinit var binding: FragmentMessagingBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatroomId: String
    private lateinit var adapter: MessagesAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var username: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessagingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve chatroom information from fragment arguments
        chatroomId = arguments?.getString("groupId") ?: ""
        val groupName = arguments?.getString("groupName") ?: "Chatroom"
        activity?.title = groupName

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Fetch current user's display name
        username = auth.currentUser?.displayName ?: "Anonymous"

        setupRecyclerView() // Initialize RecyclerView for messages
        fetchMessages() // Fetch and listen for chat messages

        // Handle sending a message
        binding.buttonSendMsg.setOnClickListener {
            val messageContent = binding.msgToSend.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                sendMessage(messageContent)
                binding.msgToSend.text.clear() // Clear input field after sending
            }
        }
    }

    // Sets up RecyclerView with an adapter and layout manager
    private fun setupRecyclerView() {
        adapter = MessagesAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    // Fetches messages from Firestore and listens for updates in real-time
    private fun fetchMessages() {
        db.collection("chatrooms").document(chatroomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Failed to load messages.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // Update messages list with the latest data
                messages.clear()
                for (doc in snapshots!!) {
                    val message = doc.toObject(Message::class.java)
                    messages.add(message)
                }

                adapter.notifyDataSetChanged() // Refresh RecyclerView
                binding.recyclerView.scrollToPosition(messages.size - 1) // Scroll to the newest message
            }
    }

    // Sends a message to Firestore chatroom
    private fun sendMessage(content: String) {
        val currentUser = auth.currentUser
        val sender = currentUser?.displayName ?: "Unknown" // Use "Unknown" if the user's display name is not available

        val message = hashMapOf(
            "sender" to sender,
            "type" to "text",
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp() // Firestore's server-side timestamp
        )

        db.collection("chatrooms").document(chatroomId)
            .collection("messages")
            .add(message)
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to send message.", Toast.LENGTH_SHORT).show()
            }
    }
}
