package fr.isen.mullot.crushisen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileEditScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    val years = listOf("N1", "N2", "N3", "M1", "M2")
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Charger les données de l'utilisateur
    LaunchedEffect(key1 = true) {
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                username = document.getString("username") ?: ""
                email = document.getString("email") ?: ""
                bio = document.getString("bio") ?: ""
                phone = document.getString("phone") ?: ""
                year = document.getString("year") ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profile") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            TextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            TextField(value = bio, onValueChange = { bio = it }, label = { Text("Bio") })
            TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = {
                    dropdownExpanded = !dropdownExpanded
                }
            ) {
                TextField(
                    readOnly = true,
                    value = year,
                    onValueChange = { },
                    label = { Text("Year") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = dropdownExpanded
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = {
                        dropdownExpanded = false
                    }
                ) {
                    years.forEach { yearOption ->
                        DropdownMenuItem(
                            text = { Text(yearOption) },
                            onClick = {
                                year = yearOption
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "bio" to bio,
                        "phone" to phone,
                        "year" to year
                    )
                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            // Gestion du succès
                        }
                        .addOnFailureListener {
                            // Gestion de l'échec
                        }
                }
            ) {
                Text("Save")
            }
        }
    }
}
