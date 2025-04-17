package com.example.makeitmeme.navigation

import android.util.Log
import androidx.compose.runtime.*
import com.example.makeitmeme.AuthManager
import com.example.makeitmeme.ui.auth.AuthScreen
import com.example.makeitmeme.ui.main.HomeScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun AuthNavigator() {
    val auth = AuthManager.auth
    var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

    // Écouteur pour surveiller les changements d’état de connexion
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            currentUser = user
            Log.d("AuthNavigator", "Auth state changed: ${user?.uid ?: "déconnecté"}")
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
            Log.d("AuthNavigator", "AuthStateListener detached")
        }
    }

    // Navigation conditionnelle
    if (currentUser == null) {
        AuthScreen(
            auth = auth,
            onAuthComplete = { user ->
                Log.d("AuthNavigator", "Connexion réussie : ${user.uid}")
                currentUser = user // Facultatif, car listener le fait
            }
        )
    } else {
        HomeScreen(
            user = currentUser!!,
            onLogout = {
                Log.d("AuthNavigator", "Déconnexion demandée")
                auth.signOut()
            }
        )
    }
}
