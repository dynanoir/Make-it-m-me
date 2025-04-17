package com.example.makeitmeme.navigation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.makeitmeme.AuthManager
import com.example.makeitmeme.ui.auth.AuthScreen
import com.example.makeitmeme.ui.main.HomeScreen
import com.example.makeitmeme.ui.main.MenuScreen
import com.example.makeitmeme.ui.main.RealtimeDatabaseSection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

enum class Screen {
    MENU, JEU, CHAT
}

@Composable
fun AuthNavigator() {
    val auth = AuthManager.auth
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
    var currentScreen by remember { mutableStateOf(Screen.MENU) }

    // Écouteur d'état d'authentification
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            auth = auth,
            onAuthComplete = { user ->
                currentUser = user
            }
        )
    } else {
        when (currentScreen) {
            Screen.MENU -> MenuScreen(
                user = currentUser!!,
                onPlay = { currentScreen = Screen.JEU },
                onChat = { currentScreen = Screen.CHAT },
                onLogout = {
                    auth.signOut()
                }
            )

            Screen.JEU -> HomeScreen(
                user = currentUser!!,
                onLogout = {
                    auth.signOut()
                    currentScreen = Screen.MENU
                }
            )

            Screen.CHAT -> ChatScreen(
                user = currentUser!!,
                onBackToMenu = {
                    currentScreen = Screen.MENU
                }
            )
        }
    }
}

@Composable
fun ChatScreen(
    user: FirebaseUser,
    onBackToMenu: () -> Unit
) {
    val ref = FirebaseDatabase.getInstance()
        .reference.child("messages").child(user.uid)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            RealtimeDatabaseSection(userMessageReference = ref)
            Button(onClick = onBackToMenu) {
                Text("⬅️ Retour au menu")
            }
        }
    }
}
