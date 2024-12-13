package com.example.stech_buddies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Navigating between chatrooms
class ChatroomFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatroomsRecyclerView: RecyclerView // RecyclerView display chatrooms
    private lateinit var chatroomAdapter: ChatroomAdapter // Adapter RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chatroom, container, false)

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // RecyclerView with LinearLayoutManager
        chatroomsRecyclerView = view.findViewById(R.id.chatroomsRecyclerView)
        chatroomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with empty list and set up click listener
        chatroomAdapter = ChatroomAdapter(emptyList()) { chatroomId, chatroomName ->
            navigateToChatroom(chatroomId, chatroomName) // Navigate to MessagingFragment on click
        }
        chatroomsRecyclerView.adapter = chatroomAdapter

        // Fetch and display the user's chatrooms
        fetchUserGroups()

        return view
    }

    // Fetches list of chatrooms the current user belongs to (Firestore)
    private fun fetchUserGroups() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { userDocument ->
                    val username = userDocument.getString("username") ?: "Anonymous" // Get username or fallback to Anonymous

                    // Query chatrooms where the user is member
                    db.collection("groups").whereArrayContains("members", username).get()
                        .addOnSuccessListener { groupDocuments ->
                            val chatrooms = groupDocuments.map { document ->
                                Chatroom(
                                    id = document.id,
                                    name = document.getString("name") ?: "Unnamed Chatroom" // Fallback for missing names
                                )
                            }
                            chatroomAdapter.updateChatrooms(chatrooms) // Update the RecyclerView with fetched chatrooms
                        }
                        .addOnFailureListener {
                            // Error handling
                            Toast.makeText(requireContext(), "Failed to load chatrooms", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    // Error handling
                    Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Error handling
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    // Navigates to MessagingFragment for selected chatroom
    private fun navigateToChatroom(chatroomId: String, chatroomName: String) {
        val fragment = MessagingFragment().apply {
            arguments = Bundle().apply {
                putString("groupId", chatroomId) // Pass chatroom ID to fragment
                putString("groupName", chatroomName) // Pass chatroom name to fragment
            }
        }
        // Replace current fragment with MessagingFragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Allow navigation back
            .commit()
    }
}

// Data class representing chatroom
data class Chatroom(
    val id: String, // Chatroom ID
    val name: String // Chatroom name
)

// Adapter displaying chatroom data in RecyclerView
class ChatroomAdapter(
    private var chatrooms: List<Chatroom>, // List of chatrooms to display
    private val onClick: (String, String) -> Unit // Callback for handling item clicks
) : RecyclerView.Adapter<ChatroomAdapter.ChatroomViewHolder>() {

    // ViewHolder bind chatroom data to the UI
    class ChatroomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatroomName: TextView = view.findViewById(R.id.groupName) // TextView for chatroom name
    }

    // Inflate item layout and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false) // Reuse `item_group` layout
        return ChatroomViewHolder(view)
    }

    // Bind chatroom data to ViewHolder for a specific position
    override fun onBindViewHolder(holder: ChatroomViewHolder, position: Int) {
        val chatroom = chatrooms[position]
        holder.chatroomName.text = chatroom.name
        holder.itemView.setOnClickListener { onClick(chatroom.id, chatroom.name) } // Handle item click
    }

    // Return the total number chatrooms in the list
    override fun getItemCount(): Int = chatrooms.size

    // Update the list of chatrooms and refresh the UI
    fun updateChatrooms(newChatrooms: List<Chatroom>) {
        chatrooms = newChatrooms
        notifyDataSetChanged() // Notify RecyclerView to refresh data
    }
}
