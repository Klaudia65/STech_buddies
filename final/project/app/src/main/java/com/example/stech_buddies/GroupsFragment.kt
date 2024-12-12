package com.example.stech_buddies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var majorSpinner: Spinner
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var createGroupButton: Button
    private lateinit var groupAdapter: GroupAdapter

    private val majors = arrayOf(
        "Architectural Design", "Architectural Engineering", "Architecture",
        "Business Administration", "Chemical and Biomolecular Engineering",
        "Civil Engineering", "Computer Science and Engineering",
        "Electrical & Information Engineering", "Electronic Engineering",
        "English Language & Literature", "Fine Arts", "Global Technology Management",
        "Industrial Design", "IT Management", "Liberal Arts",
        "Materials Science & Engineering", "Mechanical Engineering",
        "Media", "Metal Arts & Design", "Physics", "Public Administration"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        majorSpinner = view.findViewById(R.id.majorSpinner)
        groupsRecyclerView = view.findViewById(R.id.groupsRecyclerView)
        createGroupButton = view.findViewById(R.id.createGroupButton)

        groupsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        groupAdapter = GroupAdapter(emptyList()) { groupId ->
            showGroupDetailsDialog(groupId)
        }
        groupsRecyclerView.adapter = groupAdapter

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            majors
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        majorSpinner.adapter = adapter

        majorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMajor = majors[position]
                fetchGroups(selectedMajor)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        createGroupButton.setOnClickListener {
            showCreateGroupDialog()
        }

        return view
    }

    private fun fetchGroups(major: String) {
        db.collection("groups").whereEqualTo("major", major).get()
            .addOnSuccessListener { documents ->
                val groups = documents.map { document ->
                    Group(
                        id = document.id,
                        name = document.getString("name") ?: "Unnamed Group",
                        major = major
                    )
                }
                groupAdapter.updateGroups(groups)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load groups", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showGroupDetailsDialog(groupId: String) {
        db.collection("groups").document(groupId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val groupName = document.getString("name") ?: "Unnamed Group"
                    val createdBy = document.getString("createdBy") ?: "Unknown"
                    val members = document.get("members") as? List<String> ?: emptyList()

                    val currentUser = auth.currentUser
                    val userId = currentUser?.uid

                    if (userId != null) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                val username = userDoc.getString("username") ?: "Anonymous"
                                val isMember = members.contains(username)

                                val builder = AlertDialog.Builder(requireContext())
                                    .setTitle("Group Details")
                                    .setMessage(
                                        """
                                        Group Name: $groupName
                                        Created By: $createdBy
                                        Members: ${members.joinToString(", ")}
                                        """.trimIndent()
                                    )

                                if (isMember) {
                                    builder.setPositiveButton("Leave") { _, _ ->
                                        leaveGroup(groupId, username)
                                    }
                                } else {
                                    builder.setPositiveButton("Join") { _, _ ->
                                        joinGroup(groupId, username)
                                    }
                                }

                                builder.setNegativeButton("Cancel", null)
                                builder.create().show()
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load group details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun joinGroup(groupId: String, username: String) {
        db.collection("groups").document(groupId)
            .update("members", FieldValue.arrayUnion(username))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "You joined the group!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to join group", Toast.LENGTH_SHORT).show()
            }
    }

    private fun leaveGroup(groupId: String, username: String) {
        db.collection("groups").document(groupId)
            .update("members", FieldValue.arrayRemove(username))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "You left the group!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to leave group", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCreateGroupDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_group, null)
        val groupNameInput = dialogView.findViewById<EditText>(R.id.groupNameInput)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Create Group")
            .setPositiveButton("Create") { _, _ ->
                val groupName = groupNameInput.text.toString()
                if (groupName.isNotEmpty()) {
                    val selectedMajor = majorSpinner.selectedItem.toString()
                    createGroup(groupName, selectedMajor)
                } else {
                    Toast.makeText(requireContext(), "Enter a group name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)

        builder.create().show()
    }

    private fun createGroup(name: String, major: String) {
        db.collection("groups")
            .whereEqualTo("name", name)
            .whereEqualTo("major", major)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(requireContext(), "A group with this name already exists in this major!", Toast.LENGTH_SHORT).show()
                } else {
                    val currentUser = auth.currentUser
                    val userId = currentUser?.uid

                    if (userId != null) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                val username = document.getString("username") ?: "Anonymous"

                                val group = hashMapOf(
                                    "name" to name,
                                    "major" to major,
                                    "createdBy" to username,
                                    "members" to listOf(username)
                                )

                                db.collection("groups").add(group)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Group created successfully!", Toast.LENGTH_SHORT).show()
                                        fetchGroups(major)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Failed to create group", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error checking group name. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }
}

data class Group(
    val id: String,
    val name: String,
    val major: String
)

class GroupAdapter(
    private var groups: List<Group>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupName: TextView = view.findViewById(R.id.groupName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.groupName.text = group.name
        holder.itemView.setOnClickListener { onClick(group.id) }
    }

    override fun getItemCount() = groups.size

    fun updateGroups(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
