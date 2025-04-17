package com.example.makeitmeme.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.makeitmeme.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

@Composable
fun HomeScreen(user: FirebaseUser, onLogout: () -> Unit) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var topText by remember { mutableStateOf("TOP TEXT") }
    var bottomText by remember { mutableStateOf("BOTTOM TEXT") }
    val context = LocalContext.current

    // Firebase Refs
    val storageRef = FirebaseStorage.getInstance().reference
    val databaseRef = remember {
        FirebaseDatabase.getInstance().getReference("memes").child(user.uid)
    }

    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Boutons d'action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text(stringResource(R.string.import_image))
            }
            Button(onClick = {
                selectedImageUri?.let { uri ->
                    uploadAndSaveMeme(uri, topText, bottomText, context, storageRef, databaseRef)
                }
            }) {
                Text(stringResource(R.string.save_meme))
            }
        }

        // Aperçu du meme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            selectedImageUri?.let { uri ->
                val bitmap = remember {
                    context.contentResolver.loadImageBitmap(uri)
                }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Meme Preview",
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = topText,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        shadow = Shadow(color = Color.Black, blurRadius = 8f)
                    ),
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                Text(
                    text = bottomText,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        shadow = Shadow(color = Color.Black, blurRadius = 8f)
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // Champs de texte
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
    }
}

// Extension pour charger une image
fun Context.loadImageBitmap(uri: Uri): ImageBitmap {
    val inputStream = contentResolver.openInputStream(uri)
    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
    inputStream?.close()
    return bitmap.asImageBitmap()
}

// Upload vers Firebase Storage + sauvegarde dans Realtime DB
private fun uploadAndSaveMeme(
    uri: Uri,
    topText: String,
    bottomText: String,
    context: Context,
    storageRef: StorageReference,
    databaseRef: DatabaseReference
) {
    val file = File(context.cacheDir, "meme_temp.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    val uploadTask = storageRef.child("memes/${file.name}").putFile(uri)
    uploadTask.addOnSuccessListener { taskSnapshot ->
        taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
            databaseRef.push().setValue(
                hashMapOf(
                    "imageUrl" to downloadUri.toString(),
                    "topText" to topText,
                    "bottomText" to bottomText,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }
}