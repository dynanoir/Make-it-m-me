package com.example.makeitmeme.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.compose.foundation.shape.RoundedCornerShape


data class ChatMessage(
    val user: String = "",
    val text: String = ""
)

@Composable
fun RealtimeDatabaseSection() {
    val messageRef = FirebaseDatabase.getInstance().getReference("messages")
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }

    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Moi"

    Column(modifier = Modifier.fillMaxSize()) {

        // ✅ Liste des messages stylée
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.user == currentUserEmail

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 3.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            if (!isMe) {
                                Text(
                                    text = msg.user,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMe)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        // ✅ Zone de saisie + bouton d’envoi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Écris ton message") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (messageText.isNotBlank()) {
                    val newMessage = ChatMessage(user = currentUserEmail, text = messageText)

                    // Ajout local immédiat
                    messages = messages + newMessage

                    // Envoi dans Firebase
                    messageRef.push().setValue(newMessage)

                    messageText = ""
                }
            }) {
                Text("Envoyer")
            }
        }

        // ✅ Synchronisation Firebase en temps réel
        DisposableEffect(Unit) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val msgList = mutableListOf<ChatMessage>()
                    for (msgSnap in snapshot.children) {
                        val msg = msgSnap.getValue(ChatMessage::class.java)
                        if (msg != null) {
                            msgList.add(msg)
                        }
                    }
                    messages = msgList
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Firebase error: ${error.message}")
                }
            }

            messageRef.addValueEventListener(listener)

            onDispose {
                messageRef.removeEventListener(listener)
            }
        }
    }
}
