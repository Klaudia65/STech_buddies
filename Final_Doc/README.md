/*
======================================
STech Buddies Mobile Application
======================================

Overview:
STech Buddies is an Android application designed to enhance student productivity, collaboration, and learning. It includes modules for user authentication, group management, real-time chat, study timers, AI tutoring, and study progress tracking.

--------------------------------------

Key Features:
1. User Authentication:
   - Secure login and registration using Firebase Authentication.
   - Profiles include name, email, major, and profile picture.

2. Home Page:
   - Displays user details and real-time weather updates.
   - Event calendar for planning group meetings.

3. Groups Management:
   - Create or join study groups categorized by majors.
   - View group details such as members and creators.
   - Options to join or leave groups dynamically.

4. Chatrooms:
   - Real-time messaging for group collaboration.
   - Supports timestamps and stores data in Firebase Firestore.

5. AI Tutoring:
   - Utilizes OpenAI's GPT model for academic tutoring.
   - Users can ask questions and receive AI responses.

6. Study Timer:
   - Pomodoro-style timer with study and break intervals.
   - Notifies users upon session completion via sound/vibration.

7. Statistics:
   - Tracks monthly study progress and provides motivational feedback.
   - Displays past performance visually with progress bars.

--------------------------------------

Technologies Used:
1. Firebase:
   - Authentication for secure login and sign-up.
   - Firestore for storing user profiles, groups, and chat data.
   - Cloud Messaging for notifications.

2. OpenAI GPT API:
   - Enables AI tutoring functionality.

3. OpenWeather API:
   - Fetches real-time weather updates for the home screen.

4. Libraries:
   - Retrofit: For API calls (REST).
   - Glide: Efficient image loading.
   - MaterialCalendarView: Calendar UI integration.
   - RecyclerView: Dynamic list display.

--------------------------------------

App Architecture:
- Model-View-ViewModel (MVVM):
  - Separates UI from business logic for better maintainability.
  - Model: Manages data (e.g., user profiles, messages).
  - View: XML layouts for UI components.
  - ViewModel: Connects View and Model, managing state and logic.

- Firebase Integration:
  - Centralized real-time database.
  - Handles user sessions and authentication.

--------------------------------------

Module-Wise Functionalities:

1. Sign In / Sign Up:
   - Firebase Authentication for user login and registration.
   - Validates input fields for security.

2. Home Page:
   - Displays user details and profile photo.
   - Fetches weather updates via OpenWeather API.
   - Event planning with MaterialCalendarView.

3. Groups:
   - Create or join groups based on majors.
   - Real-time updates for group details using Firebase Firestore.

4. Chatrooms:
   - Real-time messaging system with RecyclerView for chat display.
   - Data stored and retrieved using Firestore.

5. AI Tutoring:
   - Integrates OpenAI GPT API for academic assistance.
   - Displays AI responses in real-time.

6. Study Timer:
   - Customizable timers for study/break sessions.
   - Sends notifications for session completion.

7. Statistics:
   - Tracks study hours and progress.
   - Visualizes progress with bars and motivational messages.

--------------------------------------

Future Enhancements:
- Voice input for AI tutoring.
- File sharing in chatrooms.
- Advanced event management options.

======================================
