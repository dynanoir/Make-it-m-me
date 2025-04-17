package com.example.makeitmeme.ui.main

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

@Composable
fun HomeScreen(user: FirebaseUser, onLogout: () -> Unit) {
    val context = LocalContext.current
    val memeBitmap = BitmapFactory.decodeResource(
        context.resources,
        com.example.makeitmeme.R.drawable.meme1
    )

    var topText by remember { mutableStateOf("") }
    var bottomText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Bienvenue ${user.email}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            bitmap = memeBitmap.asImageBitmap(),
            contentDescription = "Meme",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = topText,
            onValueChange = { topText = it },
            label = { Text("Texte du haut") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bottomText,
            onValueChange = { bottomText = it },
            label = { Text("Texte du bas") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Future action: sauvegarde ou génération
        }) {
            Text("Générer / Sauvegarder le mème")
        }

        Spacer(modifier = Modifier.height(32.dp))

        val dbRef = FirebaseDatabase.getInstance()
            .reference.child("messages").child(user.uid)

        RealtimeDatabaseSection(userMessageReference = dbRef)

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Déconnexion")
        }
    }
}
