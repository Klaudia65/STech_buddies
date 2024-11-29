package com.example.stech_buddies

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.stech_buddies.fragments.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomePageActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Update nav header with user full name
        updateNavHeader(navigationView)

        // Default Fragment: Home
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), "Home")
            navigationView.setCheckedItem(R.id.nav_home)
        }

        // Navigation Click Listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment(), "Home")
                R.id.nav_groups -> replaceFragment(GroupsFragment(), "Groups")
                R.id.nav_chatroom -> replaceFragment(ChatroomFragment(), "Chatroom")
                R.id.nav_tutoring -> replaceFragment(TutoringFragment(), "Tutoring")
                R.id.nav_timer -> replaceFragment(TimerFragment(), "Timer")
                R.id.nav_map -> replaceFragment(MapFragment(), "Map")
                R.id.nav_stats -> replaceFragment(StatsFragment(), "Stats")
                R.id.nav_disconnect -> {
                    // disconnect
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        setTitle(title)
    }

    private fun updateNavHeader(navigationView: NavigationView) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val headerView = navigationView.getHeaderView(0)
            val navHeaderText = headerView.findViewById<TextView>(R.id.nav_header_text)

            // Get user full name from Firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val fullName = document.getString("fullName") ?: "User"
                        navHeaderText.text = "Welcome, $fullName!"
                    }
                }
                .addOnFailureListener { exception ->
                    navHeaderText.text = "Welcome!"
                }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
