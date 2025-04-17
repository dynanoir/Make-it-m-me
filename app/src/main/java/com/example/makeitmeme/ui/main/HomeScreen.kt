package com.example.makeitmeme.ui.main

import androidx.compose.runtime.remember
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.makeitmeme.ui.main.RealtimeDatabaseSection
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

private const val URL_RTDB = "https://make-it-meme-1f1b2-default-rtdb.europe-west1.firebasedatabase.app/"
@Composable
fun HomeScreen(user: FirebaseUser, onLogout: () -> Unit) {
    // --- AJOUT CREATION REFERENCE ---
    // Utilise remember pour optimiser la création de la référence
    val userMessageRef: DatabaseReference = remember(user.uid) {
        FirebaseDatabase.getInstance(URL_RTDB) // Utilise la constante URL
            .getReference("userMessages")
            .child(user.uid)
            .child("message")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connecté !",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Affiche email ou UID si email non dispo (ex: auth anonyme)
        Text(
            text = "Email: ${user.email ?: "Non disponible"}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "UID: ${user.uid}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(24.dp)) // Ajusté le Spacer
        RealtimeDatabaseSection(userMessageReference = userMessageRef)
        Spacer(modifier = Modifier.weight(1f)) // Pousse le bouton en bas

        // Bouton Déconnexion
        Button(onClick = onLogout) {
            Text("Se déconnecter")
        }
    }
}