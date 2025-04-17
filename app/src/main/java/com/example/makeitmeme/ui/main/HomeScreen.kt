package com.example.makeitmeme.ui.main

import android.graphics.*
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
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
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

@Composable
fun HomeScreen(user: FirebaseUser, onLogout: () -> Unit) {
    val context = LocalContext.current
    val memeImages = listOf(
        com.example.makeitmeme.R.drawable.meme1,
        com.example.makeitmeme.R.drawable.meme2,
        com.example.makeitmeme.R.drawable.meme3
    )
    val randomImageRes = remember {
        memeImages[Random.nextInt(memeImages.size)]
    }
    val memeBitmap = remember {
        BitmapFactory.decodeResource(context.resources, randomImageRes)
    }
    var topText by remember { mutableStateOf("") }
    var bottomText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Bienvenue ${user.email}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = memeBitmap.asImageBitmap(),
                contentDescription = "Meme",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = topText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = bottomText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

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
            val finalBitmap = generateMemeBitmap(memeBitmap, topText, bottomText)
            val savedFile = saveBitmapToGallery(context, finalBitmap)
            if (savedFile != null) {
                Toast.makeText(context, "Image sauvegardée : ${savedFile.absolutePath}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Générer / Sauvegarder le mème")
        }

        Spacer(modifier = Modifier.height(32.dp))

        val dbRef = FirebaseDatabase.getInstance()
            .reference.child("messages").child(user.uid)

        RealtimeDatabaseSection()

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Déconnexion")
        }
    }
}

fun generateMemeBitmap(baseBitmap: Bitmap, topText: String, bottomText: String): Bitmap {
    val result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val paint = Paint().apply {
        color = Color.WHITE
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        setShadowLayer(5f, 0f, 0f, Color.BLACK)
    }

    val x = canvas.width / 2f
    canvas.drawText(topText.uppercase(), x, 100f, paint)
    canvas.drawText(bottomText.uppercase(), x, canvas.height - 50f, paint)

    return result
}

fun saveBitmapToGallery(context: android.content.Context, bitmap: Bitmap): File? {
    val filename = "meme_${System.currentTimeMillis()}.png"
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    if (!picturesDir.exists()) picturesDir.mkdirs()

    val file = File(picturesDir, filename)
    return try {
        val output = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        output.flush()
        output.close()

        // Afficher l’image dans la galerie (Photos)
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.absolutePath),
            arrayOf("image/png"),
            null
        )

        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
