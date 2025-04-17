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

// Enum pour la navigation entre les écrans
enum class Screen {
    MENU, JEU, CHAT
}

@Composable
fun AuthNavigator() {
    val auth = AuthManager.auth
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }
    var currentScreen by remember { mutableStateOf(Screen.MENU) }

    // Observer les changements de connexion Firebase
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // Navigation entre les écrans
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
                onBackToMenu = {
                    currentScreen = Screen.MENU
                }
            )
        }
    }
}

// ✅ ChatScreen avec affichage du chat et bouton retour
@Composable
fun ChatScreen(onBackToMenu: () -> Unit) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text("⬅️ Retour au menu")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("💬 Chat en ligne", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // 🧠 Le chat (il scrolle sans cacher le bouton)
            RealtimeDatabaseSection()
        }
    }
}
