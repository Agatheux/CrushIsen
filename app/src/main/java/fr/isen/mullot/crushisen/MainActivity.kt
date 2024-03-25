package fr.isen.mullot.crushisen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialiser Firebase
        FirebaseApp.initializeApp(this)
        setContent {
            CrushIsenTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            LoginPage(navController)
        }
        composable("createAccount") {
            CreateAccountPage(navController)
        }
    }
}

fun navigateToCreateAccountScreen(navController: NavController) {
    navController.navigate("createAccount")
}

@Composable
fun CreateAccountPage(navController: NavController) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Déclaration des variables pour stocker les valeurs des champs de texte
    var nomValue by remember { mutableStateOf("") }
    var prenomValue by remember { mutableStateOf("") }
    var adresseValue by remember { mutableStateOf("") }
    var dateNaissanceValue by remember { mutableStateOf("") }
    var anneeLisenValue by remember { mutableStateOf("") }
    var numeroValue by remember { mutableStateOf("") }
    var descriptionValue by remember { mutableStateOf("") }
    var pseudoValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            photoUri = uri
            // Charge la photo sélectionnée
            loadPhoto(context, photoUri) { bitmap ->
                imageBitmap = bitmap
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        item {
            Text(
                text = "Création de compte",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Champ pour le nom
        item {
            OutlinedTextField(
                value = nomValue,
                onValueChange = { nomValue = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Champ pour le prénom
        item {
            OutlinedTextField(
                value = prenomValue,
                onValueChange = { prenomValue = it },
                label = { Text("Prénom") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        // Champ pour l'email
        item {
            OutlinedTextField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
        }


        // Champ pour l'adresse
        item {
            OutlinedTextField(
                value = adresseValue,
                onValueChange = { adresseValue = it },
                label = { Text("Adresse") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Champ pour la date de naissance
        item {
            OutlinedTextField(
                value = dateNaissanceValue,
                onValueChange = { dateNaissanceValue = it },
                label = { Text("Date de naissance") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Champ pour l'année à l'ISEN
        item {
            OutlinedTextField(
                value = anneeLisenValue,
                onValueChange = { anneeLisenValue = it },
                label = { Text("Année à l'ISEN") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Champ pour le numéro de téléphone
        item {
            OutlinedTextField(
                value = numeroValue,
                onValueChange = { numeroValue = it },
                label = { Text("Numéro de téléphone") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Champ pour la description
        item {
            OutlinedTextField(
                value = descriptionValue,
                onValueChange = { descriptionValue = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
        }    // Champ pour le pseudo
        item {
            OutlinedTextField(
                value = pseudoValue,
                onValueChange = { pseudoValue = it },
                label = { Text("Pseudo") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Champ pour télécharger une photo
        item {
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Télécharger une photo")
            }
        }

        // Affichage de la photo sélectionnée
        imageBitmap?.let { bitmap ->
            item {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Photo sélectionnée",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(120.dp),
                )
            }
        }

        // Bouton pour soumettre le formulaire
        item {
            Button(
                onClick = {
                    val user = User(
                        adresse = adresseValue,
                        annee_a_lisen = anneeLisenValue,
                        date_naissance = dateNaissanceValue,
                        description = descriptionValue,
                        email = emailValue,
                        nom = nomValue,
                        numero = numeroValue,
                        prenom = prenomValue,
                        pseudo = pseudoValue
                    )
                    saveUserToFirebase(context = context, user = user, photoUri = photoUri)
                    // Vérifiez si photoUri n'est pas nulle avant d'appeler la fonction
                    photoUri?.let { uri ->
                        uploadImageToFirebaseStorage(context = context, imageUri = uri)
                    }
                    showDialog = true // Afficher le dialog après la création du compte
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Valider")
            }
        }
    }

        // Bloc pour afficher l'AlertDialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.navigateUp() // Naviguer vers la première page
            },
            title = { Text("Compte créé avec succès") },
            text = { Text("Votre compte a été créé avec succès.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigateUp() // Naviguer vers la première page
                    }
                ) {
                    Text("OK")
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

data class User(
    val adresse: String = "",
    val annee_a_lisen: String = "",
    val date_naissance: String = "",
    val description: String = "",
    val email: String = "",
    val nom: String = "",
    val numero: String = "",
    val prenom: String = "",
    val pseudo: String = ""
)

fun saveUserToFirebase(context: Context, user: User, photoUri: Uri?) {
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("Crushisen").child("user")
    val userId = usersRef.push().key // Génère une clé unique pour l'utilisateur
    userId?.let { uid ->
        // Enregistrer l'utilisateur dans la base de données avec le chemin de l'image
        usersRef.child(uid).setValue(user)
            .addOnSuccessListener {
                Log.d("Firebase", "User added successfully: $user")

                // Si une photo a été sélectionnée et téléchargée avec succès
                if (photoUri != null) {
                    // Obtenir une référence à Firebase Storage
                    val storage = Firebase.storage
                    val storageRef = storage.reference

                    // Nom de fichier unique pour éviter les conflits
                    val filename = "${UUID.randomUUID()}.jpg"
                    val imageRef = storageRef.child("images/$filename")

                    // Télécharger l'image vers Firebase Storage
                    imageRef.putFile(photoUri)
                        .addOnSuccessListener { _ ->
                            // Récupérer l'URL de téléchargement de l'image
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                val photoUrl = uri.toString()

                                // Mettre à jour l'URL de l'image dans les données de l'utilisateur
                                usersRef.child(uid).child("photoUrl").setValue(photoUrl)
                                    .addOnSuccessListener {
                                        Log.d("Firebase", "Photo URL added successfully for user: $uid")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firebase", "Failed to add photo URL for user: $uid - $e")
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to upload image: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to add user: $e")
            }
    }
}


@Composable
fun LoginPage(navController: NavController = rememberNavController()) {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp)            )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { /* Handle login button click */ },
                    modifier = Modifier.padding(8.dp),
                    content = {
                        Text(
                            "Se connecter",
                            color = Color.White // Couleur du texte blanc
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navigateToCreateAccountScreen(navController) }, // Navigation vers la page de création de compte
                    modifier = Modifier.padding(8.dp),
                    content = {
                        Text(
                            "Créer un compte",
                            color = Color.White // Couleur du texte blanc
                        )
                    }
                )
            }
        }
    }
}

fun loadPhoto(context: Context, uri: Uri?, onBitmapLoaded: (ImageBitmap) -> Unit) {
    uri?.let { selectedUri ->
    // Utilisation de Glide pour charger la photo depuis l'URI
        Glide.with(context)
            .asBitmap()
            .load(selectedUri)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            // Appelle la fonction de rappel avec le bitmap chargé
                    onBitmapLoaded(resource.asImageBitmap())
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Ne rien faire
                }
            })
    }
}

fun uploadImageToFirebaseStorage(context: Context, imageUri: Uri) {
    val storage = Firebase.storage
    val storageRef = storage.reference

    // Nom de fichier unique pour éviter les conflits
    val filename = "${UUID.randomUUID()}.jpg"
    val imageRef = storageRef.child("images/$filename")

    val uploadTask = imageRef.putFile(imageUri)

    uploadTask.addOnSuccessListener { taskSnapshot ->
        // Récupérer l'URL de téléchargement de l'image
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            // Enregistrer l'URL dans Firebase Realtime Database ou Firestore
            val imageUrl = uri.toString()
            Log.d("Firebase", "Image uploaded successfully. URL: $imageUrl")
            // Ici, vous pouvez enregistrer l'URL dans la base de données
        }
    }.addOnFailureListener { exception ->
        Log.e("Firebase", "Failed to upload image: $exception")
    }
}
