
package fr.isen.mullot.crushisen

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Scaffold
import androidx.compose.ui.Alignment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance()
    //   val userId = auth.currentUser?.uid ?: ""

    val userId = "NtteAKPqJKUjejLcfbe"

    //  val userRef = db.getReference().child("Crushisen").child("user")
    val userRef = db.getReference().child("Crushisen/user").child("-NttfMPa_iu22Z85a5WZ") // Remplacer par l'ID utilisateur correct

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") } // Si vous affichez le mot de passe actuel
    var pseudo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var annee_a_lisen by remember { mutableStateOf("") }

    LaunchedEffect(key1 = userId) {
        if (userId.isNotEmpty()) {
            userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val userData = snapshot.value as? Map<*, *>
                    userData?.let {
                        pseudo = it["pseudo"].toString()
                        email = it["email"].toString()
                        description = it["description"].toString()
                        phone = it["numero"].toString()
                        annee_a_lisen = it["annee_a_lisen"].toString()
                        adresse = it["adresse"].toString()
                        dateNaissance = it["date_naissance"].toString()
                        prenom = it["prenom"].toString()
                        nom = it["nom"].toString()


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileEditScreen", "Failed to load user data: $error")
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = "Failed to load user data.",
                            duration = androidx.compose.material.SnackbarDuration.Short
                        )
                    }
                }

            })

        }
    }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Edit Profile") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = pseudo, onValueChange = { pseudo = it }, label = { Text("Pseudo") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            OutlinedTextField(value = annee_a_lisen, onValueChange = { annee_a_lisen = it }, label = { Text("Année a l'ISEN") })
            OutlinedTextField(value = adresse, onValueChange = { adresse = it }, label = { Text("Adresse") })
            OutlinedTextField(value = dateNaissance, onValueChange = { dateNaissance = it }, label = { Text("Date de Naissance") })
            OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prenom") })
            OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") })


            Button(
                onClick = {
                    val userMap = hashMapOf(
                        "pseudo" to pseudo,
                        "email" to email,
                        "description" to description,
                        "numero" to phone,
                        "annee_a_lisen" to annee_a_lisen,
                        "adresse" to adresse,
                        "date_naissance" to dateNaissance,
                        "prenom" to prenom,
                        "nom" to nom
                    )
                    userRef.setValue(userMap).addOnSuccessListener {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = "Profile updated successfully",
                                duration = androidx.compose.material.SnackbarDuration.Short
                            )
                        }
                    }.addOnFailureListener {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = "Failed to update profile",
                                duration = androidx.compose.material.SnackbarDuration.Short
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