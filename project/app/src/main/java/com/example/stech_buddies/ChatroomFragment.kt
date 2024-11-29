package com.example.stech_buddies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.stech_buddies.databinding.ActivityMessagingBinding
import com.example.stech_buddies.databinding.FragmentChatroomBinding
import com.google.firebase.firestore.FirebaseFirestore

class ChatroomFragment : Fragment() {

    private lateinit var linearLayout: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChatroomBinding.inflate(inflater, container, false)
        linearLayout = binding.linearLayout
        fetchUsers()
        return binding.root
    }

    private fun fetchUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val username = document.getString("username")
                    if (username != null) {
                        addButton(username)
                    }
                }
            }
    }

    private fun addButton(username: String) {
        val button = Button(context)
        button.text = username
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.addView(button)
    }
}