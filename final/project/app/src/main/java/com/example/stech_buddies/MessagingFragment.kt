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

        chatroomId = arguments?.getString("groupId") ?: ""
        val groupName = arguments?.getString("groupName") ?: "Chatroom"
        activity?.title = groupName

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        username = auth.currentUser?.displayName ?: "Anonymous"

        setupRecyclerView()
        fetchMessages()

        binding.buttonSendMsg.setOnClickListener {
            val messageContent = binding.msgToSend.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                sendMessage(messageContent)
                binding.msgToSend.text.clear()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = MessagesAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun fetchMessages() {
        db.collection("chatrooms").document(chatroomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Failed to load messages.", Toast.LENGTH_SHORT).show()
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
        val currentUser = auth.currentUser
        val sender = currentUser?.displayName ?: "Unknown"

        val message = hashMapOf(
            "sender" to sender,
            "type" to "text",
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("chatrooms").document(chatroomId)
            .collection("messages")
            .add(message)
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to send message.", Toast.LENGTH_SHORT).show()
            }
    }

}
