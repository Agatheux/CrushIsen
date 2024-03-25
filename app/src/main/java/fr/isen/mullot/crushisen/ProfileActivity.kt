package fr.isen.mullot.crushisen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var pseudo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var year by remember { mutableIntStateOf(0) }
    var adresse by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Charger les données de l'utilisateur
    LaunchedEffect(key1 = true) {
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                pseudo = document.getString("pseudo") ?: ""
                email = document.getString("email") ?: ""
                description = document.getString("description") ?: ""
                phone = document.getLong("numero")?.toString() ?: ""
                year = document.getLong("annee_a_lisen")?.toInt() ?: 0
                adresse = document.getString("adresse") ?: ""
                dateNaissance = document.getString("date_naissance") ?: ""
                prenom = document.getString("prenom") ?: ""
                nom = document.getString("nom") ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profile") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            TextField(value = pseudo, onValueChange = { pseudo = it }, label = { Text("Pseudo") })
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            TextField(value = adresse, onValueChange = { adresse = it }, label = { Text("Adresse") })
            TextField(value = dateNaissance, onValueChange = { dateNaissance = it }, label = { Text("Date de Naissance") })
            TextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prenom") })
            TextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") })
            Button(
                onClick = {
                    val userMap = hashMapOf(
                        "pseudo" to pseudo,
                        "email" to email,
                        "description" to description,
                        "numero" to phone,
                        "annee_a_lisen" to year,
                        "adresse" to adresse,
                        "date_naissance" to dateNaissance,
                        "prenom" to prenom,
                        "nom" to nom

                    )
                    db.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "Profil mis à jour avec succès."
                                )
                            }
                        }
                        .addOnFailureListener { exception ->
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "Échec de la mise à jour : ${exception.localizedMessage}"
                                )
                            }
                        }
                }
            ) {
                Text("Save")
            }
        }
    }
}
