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

class ChatroomFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatroomsRecyclerView: RecyclerView
    private lateinit var chatroomAdapter: ChatroomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chatroom, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView
        chatroomsRecyclerView = view.findViewById(R.id.chatroomsRecyclerView)
        chatroomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        chatroomAdapter = ChatroomAdapter(emptyList()) { chatroomId, chatroomName ->
            navigateToChatroom(chatroomId, chatroomName)
        }
        chatroomsRecyclerView.adapter = chatroomAdapter

        fetchUserGroups()

        return view
    }

    private fun fetchUserGroups() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { userDocument ->
                    val username = userDocument.getString("username") ?: "Anonymous"

                    db.collection("groups").whereArrayContains("members", username).get()
                        .addOnSuccessListener { groupDocuments ->
                            val chatrooms = groupDocuments.map { document ->
                                Chatroom(
                                    id = document.id,
                                    name = document.getString("name") ?: "Unnamed Chatroom"
                                )
                            }
                            chatroomAdapter.updateChatrooms(chatrooms)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to load chatrooms", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToChatroom(chatroomId: String, chatroomName: String) {
        val fragment = MessagingFragment().apply {
            arguments = Bundle().apply {
                putString("groupId", chatroomId)
                putString("groupName", chatroomName)
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

data class Chatroom(
    val id: String,
    val name: String
)

class ChatroomAdapter(
    private var chatrooms: List<Chatroom>,
    private val onClick: (String, String) -> Unit
) : RecyclerView.Adapter<ChatroomAdapter.ChatroomViewHolder>() {

    class ChatroomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatroomName: TextView = view.findViewById(R.id.groupName) // Reuse group layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false) // Reuse `item_group`
        return ChatroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatroomViewHolder, position: Int) {
        val chatroom = chatrooms[position]
        holder.chatroomName.text = chatroom.name
        holder.itemView.setOnClickListener { onClick(chatroom.id, chatroom.name) }
    }

    override fun getItemCount() = chatrooms.size

    fun updateChatrooms(newChatrooms: List<Chatroom>) {
        chatrooms = newChatrooms
        notifyDataSetChanged()
    }
}
