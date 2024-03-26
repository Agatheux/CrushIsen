package fr.isen.mullot.crushisen

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme
import java.util.UUID
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest
import com.google.firebase.auth.FirebaseAuth
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

        composable("login") {
            CreateLoginPage(navController, LocalContext.current)
        }
    }
}

fun navigateToCreateAccountScreen(navController: NavController) {
    navController.navigate("createAccount")
}

fun navigateToCreateLoginScreen(navController: NavController) {
    navController.navigate("login")
}

@Composable
fun CreateAccountPage(navController: NavController) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var currentPage by remember { mutableStateOf(1) }

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
    var passwordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // État pour gérer l'affichage de l'AlertDialog
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Fonction pour afficher l'AlertDialog en cas de saisie invalide
    fun showErrorDialog(message: String) {
        errorMessage = message
        showErrorDialog = true
    }

    // Fonction pour valider que le numéro de téléphone ne contient que des chiffres
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.all { it.isDigit() }
    }
    // Fonction pour valider le format de la date de naissance (JJ/MM/AAAA)
    fun isValidDateOfBirth(date: String): Boolean {
        val datePattern = """\d{2}/\d{2}/\d{4}""".toRegex()
        return date.matches(datePattern)
    }
    // Fonction pour valider que l'utilisateur a sélectionné une image
    fun isImageSelected(photoUri: Uri?): Boolean {
        return photoUri != null
    }

    // Fonction pour valider que les champs ne sont pas vides
    fun isValidField(value: String, fieldName: String): Boolean {
        return value.isNotBlank()
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+!?=])(?=\\S+\$).{10,}$")
        return passwordPattern.matches(password)
    }

    val database = Firebase.database
    val usersRef = database.getReference("Crushisen").child("user")

    // Fonction pour vérifier si l'adresse e-mail est déjà utilisée
    fun isEmailAlreadyUsed(email: String, callback: (Boolean) -> Unit) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gérer les erreurs de base de données
                callback(false)
            }
        })
    }

    // Fonction pour vérifier si le pseudo est déjà utilisé
    fun isPseudoAlreadyUsed(pseudo: String, callback: (Boolean) -> Unit) {
        usersRef.orderByChild("pseudo").equalTo(pseudo).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gérer les erreurs de base de données
                callback(false)
            }
        })
    }



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
        item {
            Spacer(modifier = Modifier.padding(10.dp))
        }

        when (currentPage) {
            1 -> {
                // Champs pour la première partie
                item {
                    OutlinedTextField(
                        value = prenomValue,
                        onValueChange = { prenomValue = it },
                        label = { Text("Prénom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = nomValue,
                        onValueChange = { nomValue = it },
                        label = { Text("Nom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = emailValue,
                        onValueChange = { emailValue = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = passwordValue,
                        onValueChange = { passwordValue = it },
                        label = { Text("Mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = confirmPasswordValue,
                        onValueChange = { confirmPasswordValue = it },
                        label = { Text("Confirmation du mot de passe") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = dateNaissanceValue,
                        onValueChange = { dateNaissanceValue = it },
                        label = { Text("Date de naissance") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
            }
            2 -> {
                // Champs pour la deuxième partie
                item {
                    OutlinedTextField(
                        value = adresseValue,
                        onValueChange = { adresseValue = it },
                        label = { Text("Adresse") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = anneeLisenValue,
                        onValueChange = { anneeLisenValue = it },
                        label = { Text("Année à l'ISEN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = numeroValue,
                        onValueChange = { numeroValue = it },
                        label = { Text("Numéro de téléphone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = descriptionValue,
                        onValueChange = { descriptionValue = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedTextField(
                        value = pseudoValue,
                        onValueChange = { pseudoValue = it },
                        label = { Text("Pseudo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
                }
                item {
                    OutlinedButton(
                        onClick = { launcher.launch("image/*")  },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Télécharger une photo")
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(10.dp))
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
            }
        }

        // Bouton précédent pour revenir à la première partie
        if (currentPage == 2) {
            item {
                Button(
                    onClick = {
                        // Revenir à la première partie
                        currentPage = 1
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Précédent")
                }
            }
        }

        // Bouton suivant pour passer à la deuxième partie
        if (currentPage == 1) {
            item {
                Button(
                    onClick = {
                        // Vérifier les validations avant de passer à la deuxième partie
                        // Si tout est valide, passer à la deuxième partie
                        currentPage = 2
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Suivant")
                }
            }
        }

        // Bouton pour soumettre le formulaire
        item {
            Button(
                onClick = {
                    // Vérification de la sélection de l'image
                    if (isImageSelected(photoUri)) {
                        // Vérification des autres champs
                        if (isValidField(nomValue, "Nom") &&
                            isValidField(prenomValue, "Prénom") &&
                            isValidField(adresseValue, "Adresse") &&
                            isValidField(anneeLisenValue, "Année à l'ISEN") &&
                            isValidField(descriptionValue, "Description") &&
                            isValidField(pseudoValue, "Pseudo") &&
                            isValidField(emailValue, "Email") &&
                            isValidField(passwordValue, "Mot de passe") &&
                            isValidField(confirmPasswordValue, "Confirmation du mot de passe")
                        ) {
                            // Vérification de la date de naissance
                            if (isValidDateOfBirth(dateNaissanceValue)) {
                                // Vérification du numéro de téléphone
                                if (isValidPhoneNumber(numeroValue)) {
                                    // Vérification de l'e-mail en utilisant la fonction isValidEmail
                                    if (isValidEmail(emailValue)) {
                                        // Création de l'utilisateur dans Firebase Authentication
                                        Firebase.auth.createUserWithEmailAndPassword(emailValue, passwordValue)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    // L'utilisateur a été créé avec succès
                                                    val user = User(
                                                        adresse = adresseValue,
                                                        annee_a_lisen = anneeLisenValue,
                                                        date_naissance = dateNaissanceValue,
                                                        description = descriptionValue,
                                                        email = emailValue,
                                                        nom = nomValue,
                                                        numero = numeroValue,
                                                        prenom = prenomValue,
                                                        pseudo = pseudoValue,
                                                        password = passwordValue,
                                                    )

                                                    // Enregistrement de l'utilisateur dans Firestore
                                                    saveUserToFirebase(
                                                        context = context,
                                                        user = user,
                                                        photoUri = photoUri
                                                    )
                                                        // Vérification si photoUri n'est pas nulle avant d'appeler la fonction
                                                        photoUri?.let { uri ->
                                                            // Téléchargement de l'image dans Firebase Storage
                                                            uploadImageToFirebaseStorage(
                                                                context = context,
                                                                imageUri = uri
                                                            )
                                                        }


                                                    showDialog = true // Afficher le dialog après la création du compte
                                                } else {
                                                    // Une erreur s'est produite lors de la création de l'utilisateur
                                                    val errorMessage = task.exception?.message ?: "Une erreur s'est produite lors de la création de l'utilisateur."
                                                    showErrorDialog(errorMessage)
                                                    Log.e(TAG, "Error creating user: $errorMessage")
                                                }
                                            }
                                    } else {
                                        // Afficher un message d'erreur pour l'e-mail invalide
                                        showErrorDialog("Veuillez entrer une adresse e-mail valide.")
                                        Log.e("Validation", "Email invalide")
                                    }
                                } else {
                                    // Afficher un message d'erreur pour le numéro de téléphone invalide
                                    showErrorDialog("Veuillez entrer un numéro de téléphone valide.")
                                    Log.e("Validation", "Numéro de téléphone invalide")
                                }
                            } else {
                                // Afficher un message d'erreur pour la date de naissance invalide
                                showErrorDialog("Veuillez entrer une date de naissance au format JJ/MM/AAAA.")
                                Log.e("Validation", "Date de naissance invalide")
                            }
                        } else {
                            // Afficher un message d'erreur pour les champs obligatoires vides
                            showErrorDialog("Veuillez remplir tous les champs.")
                            Log.e("Validation", "Champ obligatoire vide")
                        }
                    } else {
                        // Afficher un message d'erreur pour l'image non sélectionnée
                        showErrorDialog("Veuillez sélectionner une image.")
                        Log.e("Validation", "Image non sélectionnée")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Valider")
            }
        }
    }

        // AlertDialog pour afficher le message d'erreur
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Erreur") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                }
            )
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


@Composable
fun CreateLoginPage(navController: NavController, context: Context) {
    var emailValue by remember { mutableStateOf("") } // Changer pseudoValue en emailValue
    var passwordValue by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val auth = Firebase.auth

    // Fonction pour gérer la connexion de l'utilisateur
    fun signIn() {
        auth.signInWithEmailAndPassword(emailValue, passwordValue) // Utiliser emailValue ici
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Si la connexion réussit, récupérez le pseudo de l'utilisateur à partir de la base de données Firebase
                    val currentUser = auth.currentUser
                    val uid = currentUser?.uid ?: ""
                    val userRef = FirebaseDatabase.getInstance().getReference("Crushisen").child("user").child(uid)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val pseudo = snapshot.child("pseudo").value as? String ?: ""
                            // Naviguez vers FeedActivity avec le pseudo de l'utilisateur
                            val intent = Intent(context, FeedActivity::class.java).apply {
                                putExtra("pseudo", pseudo)
                            }
                            context.startActivity(intent)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Gérer l'erreur
                        }
                    })
                } else {
                    // Si la connexion échoue, affichez un dialogue indiquant que le pseudo et le mot de passe ne correspondent pas
                    showDialog = true
                }
            }
    }

    // Dialog pour afficher un message si les informations de connexion sont incorrectes
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Erreur") },
            text = { Text("Le pseudo et le mot de passe ne correspondent pas.") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = emailValue, // Utiliser emailValue ici
            onValueChange = { emailValue = it }, // Utiliser emailValue ici
            label = { Text("Adresse e-mail") }, // Changer le libellé en "Adresse e-mail"
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        OutlinedTextField(
            value = passwordValue,
            onValueChange = { passwordValue = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Button(
            onClick = {
                // Vérifiez les informations de connexion lors du clic sur le bouton
                signIn()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Se connecter")
        }
    }
}



fun isValidEmail(email: String): Boolean {
    // Vérifie si l'email contient au moins '@' et '.'.
    return email.contains("@") && (email.contains(".com") || email.contains(".fr"))
}

fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
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
    val pseudo: String = "",
    val password: String = "",
)

fun saveUserToFirebase(context: Context, user: User, photoUri: Uri?) {
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("Crushisen").child("user")
    val auth = FirebaseAuth.getInstance()
    val userId =  auth.currentUser?.uid ?: ""

    userId?.let { uid ->
        // Hacher le mot de passe avant de l'enregistrer
        val hashedPassword = hashPassword(user.password)

        // Remplacer le mot de passe d'origine par le mot de passe haché
        val userWithHashedPassword = user.copy(password = hashedPassword)

        // Enregistrer l'utilisateur dans la base de données avec le chemin de l'image
        usersRef.child(uid).setValue(userWithHashedPassword)
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
                    onClick = { navigateToCreateLoginScreen(navController) },
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
