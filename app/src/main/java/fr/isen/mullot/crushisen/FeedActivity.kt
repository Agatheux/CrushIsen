package fr.isen.mullot.crushisen

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme
import kotlinx.coroutines.launch

data class Post(
    val id: String, // Identifiant unique du post
    val ID_user: String,
    val description: String,
    val likes: Int,
    val photos: List<String>,
    val photoUrl: String // URL de la photo de profil de l'utilisateur
)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomePage(onNavigate: () -> Unit) {
    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = onNavigate) {
                    Text("Create a Post")
                }
            }
        }
    )
}


// Fonction modifiée pour créer un post avec téléchargement d'images
fun createPost(context: Context, userId: String, description: String, imageUris: List<Uri>) {
    // Génère un ID unique pour le post basé sur le timestamp actuel
    val postId = System.currentTimeMillis().toString()

    // Chemin vers le stockage des images
    val storageRef = FirebaseStorage.getInstance().reference.child("postImages/$postId")

    val uploadedImageUrls = mutableListOf<String>()
    var uploadCount = 0  // Pour suivre le nombre d'images téléchargées

    // Boucle sur chaque URI d'image et télécharge les images sur Firebase Storage
    for (imageUri in imageUris) {
        val individualImageRef = storageRef.child("${imageUri.lastPathSegment}")
        individualImageRef.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            individualImageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()
                uploadedImageUrls.add(downloadUri)

                // Vérifie si toutes les images ont été téléchargées
                uploadCount++
                if (uploadCount == imageUris.size) {
                    // Crée un post après que toutes les images aient été téléchargées
                    val post = hashMapOf(
                        "ID_user" to userId,
                        "description" to description,
                        "like" to 0,
                        "photos" to uploadedImageUrls
                    )
                    Log.d("CreatePost", "Post apres les images download: $post")

                    // Ajoute le post à Firebase Realtime Database
                    FirebaseDatabase.getInstance().getReference("Crushisen")
                        .child("posts")
                        .child(postId)
                        .setValue(post)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to create post", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                // Gère les échecs de téléchargement
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun CreatePostPage(context: Context, onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val description = remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf(emptyList<Uri>()) } // List of URIs val launcher = rememberLauncherForActivityResult(
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUris = imageUris + it }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xfffd8487), Color(0xffb26ebe)),
        startY = 0f,
        endY = 700f
    )

    Scaffold(

        content = { padding ->
            Box(
                modifier = Modifier
                    .background(brush = gradientBackground)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Créer un post",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center)
                    OutlinedTextField(
                        value = description.value,
                        onValueChange = { description.value = it },
                        label = { Text("Description", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color.White,
                            focusedBorderColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(20.dp)

                    ) {
                        Text("Choisir une/des images", color = Color(0xffd08ae0), )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { imageUris = emptyList() }, // Set imageUris to a new empty list
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(20.dp)

                    ) {
                        Text("Retirer les images", color = Color(0xffd08ae0))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    imageUris.forEach { uri -> // Display each selected image
                        Image(
                            painter = rememberImagePainter(data = uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val userId = auth.currentUser?.uid ?: ""
                            if (imageUris.isNotEmpty()) {
                                createPost(context, userId, description.value, imageUris.map { it }) // Pass the list of URIs as strings
                                Toast.makeText(context, "Votre post est en ligne", Toast.LENGTH_SHORT).show()
                                onBack() // Navigate back after posting
                            } else {
                                Toast.makeText(context, "Veuillez selectionner une image", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(20.dp)

                    ) {
                        Text("Poster", color = Color(0xffd08ae0))
                    }
                }
            }
        }
    )
}


class FeedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FeedActivityContent()
        }
    }

    @Composable
    fun MainScreen() {
        var showCreatePostPage by remember { mutableStateOf(false) }

        if (showCreatePostPage) {
            CreatePostPage(context = this, onBack = { /*...*/ })
        } else {
            HomePage(onNavigate = { showCreatePostPage = true })
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")


    @Composable
    fun FeedActivityContent() {
        val navController = rememberNavController()
        val context = LocalContext.current

        NavHost(navController = navController, startDestination = "feed") {
            composable("feed") {
                FeedEditScreen(navController)
            }
            composable("profile") {
                ProfileEditScreen(navController)
            }
            composable("createPost") {
                CreatePostPage(context = context, onBack = { navController.popBackStack() })
            }
        }
    }


    @Composable
    fun NotificationEditScreen(navController: NavHostController) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MainHeader(userName = "YourUserName")

                Spacer(modifier = Modifier.weight(1f))

                BottomNavBar(navController = navController)
            }
        }
    }


    @Composable
    fun BottomNavBar(navController: NavHostController) {
        val selectedItem = remember { mutableStateOf("Feed") }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            selectedItem.value = when (destination.route) {
                "feed" -> "Feed"
                "profile" -> "Profile"
                "createPost" -> "CreatePost"
                else -> "Feed"
            }
        }

        fun navigateToFeed() {
            selectedItem.value = "Feed"
            navController.navigate("feed")
        }

        fun navigateToProfile() {
            selectedItem.value = "Profile"
            navController.navigate("profile")
        }

        fun navigateToCreatePost() {
            selectedItem.value = "CreatePost"
            navController.navigate("createPost")
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavigationRailItem(
                    selected = selectedItem.value == "Feed",
                    onClick = { navigateToFeed() },
                    icon = { Icon(painterResource(id = R.drawable.icon_home), contentDescription = null, Modifier.size(24.dp)) },
                )

                NavigationRailItem(
                    selected = selectedItem.value == "CreatePost",
                    onClick = { navigateToCreatePost() },
                    icon = { Icon(painterResource(id = R.drawable.icon_post), contentDescription = null, Modifier.size(24.dp)) },
                )

                NavigationRailItem(
                    selected = selectedItem.value == "Profile",
                    onClick = { navigateToProfile() },
                    icon = { Icon(painterResource(id = R.drawable.icon_settings), contentDescription = null, Modifier.size(24.dp)) },
                )
            }
        }
    }

    @Composable
fun FeedHeader(navController: NavHostController) { // Ajoutez NavHostController en paramètre
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val userRef = db.getReference("Crushisen/user").child(userId)

    var username by remember { mutableStateOf("") }
    var photoprofil by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                username = snapshot.child("pseudo").getValue(String::class.java) ?: ""
                photoprofil = snapshot.child("photoUrl").getValue(String::class.java) ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    val imagePainter = rememberImagePainter(data = photoprofil, builder = {
        crossfade(true)
    })

    val iconPainter = painterResource(id = R.drawable.icon_pp) // Ajoutez cette ligne pour charger l'icône

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween, // Ajoutez cette ligne pour espacer les éléments
        modifier = Modifier.fillMaxWidth() // Ajoutez cette ligne pour que la Row prenne toute la largeur
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(80.dp, 80.dp) // Set the size here
                .padding(start = 16.dp) // Add padding to the start
                .clip(RoundedCornerShape(16.dp)), // Clip the image with rounded corners
            contentScale = ContentScale.Crop // Crop the image
        )
        Text(
            text = username,
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 24.sp, // Increase
            fontWeight = FontWeight.Bold// the font size here
        )
        IconButton(onClick = { navController.navigate("profile") }) { // Changez Button à IconButton
            Image(
                painter = iconPainter,
                contentDescription = "Profile Button",
                modifier = Modifier
                    .size(60.dp) // Réduisez la taille de l'icône ici
                    .padding(4.dp) // Ajoutez un padding ici
                    .padding(end = 16.dp) // Ajoutez une marge à la fin ici
            )
        }
    }
}

    class FeedViewModel : ViewModel() {
        // Message to display
        var message by mutableStateOf<Map<String, String>?>(null)
            private set // allows modification only within the class

        // Reference to the Firebase database
        private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

        // Fetch data from the Firebase database
        fun fetchData() {
            database.child("test").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val typeIndicator = object : GenericTypeIndicator<HashMap<String, String>>() {}
                    val value = dataSnapshot.getValue(typeIndicator)
                    message = value ?: mapOf("No message" to "found")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching message: ${databaseError.message}")
                    message = mapOf("Error fetching message" to "")
                }
            })
        }

        // Send a message to Firebase
        fun sendMessage(key: String, value: String) {
            database.child("test").child(key).setValue(value)
        }
    }


    @SuppressLint("RememberReturnType")
    @Composable
    fun ProfileEditScreen(navController: NavHostController) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val userRef = db.getReference("Crushisen/user").child(userId)

        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordChangeError by remember { mutableStateOf("") }
        var pseudo by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var annee_a_lisen by remember { mutableStateOf("") }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        val showDialog = remember { mutableStateOf(false) }

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                selectedImageUri = uri
            }
        // Récupération des données utilisateur pour les champs auto-remplis
        LaunchedEffect(userId) {
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(User::class.java)
                    userData?.let {
                        pseudo = it.pseudo
                        email = it.email
                        description = it.description
                        phone = it.numero
                        annee_a_lisen = it.annee_a_lisen
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gérer les erreurs de récupération des données
                }
            })
        }


        LaunchedEffect(selectedImageUri) {
            selectedImageUri?.let { uri ->
                // Si une nouvelle image a été sélectionnée, téléchargez-la et mettez à jour l'URL dans la base de données
                uploadNewProfileImage(uri)
            }
        }

        val gradientBackground = Brush.linearGradient(
            colors = listOf(Color(0xfffd8487), Color(0xffb26ebe)),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                BottomNavBar(navController = navController)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .background(brush = gradientBackground)
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Modifier votre profil",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pseudo,
                            onValueChange = { pseudo = it },
                            label = { Text(text = "Pseudo", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(text = "Email", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(text = "Description", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(text = "Phone", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = annee_a_lisen,
                            onValueChange = { annee_a_lisen = it },
                            label = { Text(text = "Année a l'ISEN", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text(text = "Ancien mot de passe", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(text = "Nouveau mot de passe", color = Color.White) },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = {
                                Text(
                                    text = "Confirmer le nouveau mot de passe",
                                    color = Color.White
                                )
                            },
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = Color.White,
                                focusedBorderColor = Color.White
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bouton pour mettre à jour le profil
                        Button(
                            onClick = {
                                // Mise à jour des informations de l'utilisateur
                                val userMap = hashMapOf(
                                    "pseudo" to pseudo,
                                    "email" to email,
                                    "description" to description,
                                    "numero" to phone,
                                    "annee_a_lisen" to annee_a_lisen
                                )
                                val currentUser = auth.currentUser
                                val userEmail = currentUser?.email ?: ""
                                // Vérification si les champs de mot de passe sont remplis et correspondent
                                if (oldPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                                    if (newPassword == confirmPassword) {
                                        // Étape 1 : Re-authentification de l'utilisateur
                                        val credential =
                                            EmailAuthProvider.getCredential(userEmail, oldPassword)
                                        currentUser?.reauthenticate(credential)
                                            ?.addOnCompleteListener { reauthTask ->
                                                if (reauthTask.isSuccessful) {
                                                    // Étape 2 : Mise à jour du mot de passe
                                                    currentUser.updatePassword(newPassword)
                                                        .addOnCompleteListener { updateTask ->
                                                            if (updateTask.isSuccessful) {
                                                                // Réinitialisation des champs de mot de passe après mise à jour
                                                                oldPassword = ""
                                                                newPassword = ""
                                                                confirmPassword = ""

                                                                // Mise à jour des autres informations de l'utilisateur
                                                                userRef.updateChildren(userMap as Map<String, Any>)
                                                                    .addOnSuccessListener {
                                                                        scope.launch {
                                                                            snackbarHostState.showSnackbar(
                                                                                "Profil et mot de passe mis à jour avec succès.",
                                                                                duration = SnackbarDuration.Short
                                                                            )
                                                                        }
                                                                    }.addOnFailureListener {
                                                                        scope.launch {
                                                                            snackbarHostState.showSnackbar(
                                                                                "Erreur lors de la mise à jour des informations du profil.",
                                                                                duration = SnackbarDuration.Short
                                                                            )
                                                                        }
                                                                    }
                                                            } else {
                                                                passwordChangeError =
                                                                    "Erreur lors de la mise à jour du mot de passe."
                                                            }
                                                        }
                                                } else {
                                                    passwordChangeError =
                                                        "L'ancien mot de passe est incorrect."
                                                }
                                            }
                                    } else {
                                        passwordChangeError =
                                            "Les nouveaux mots de passe ne correspondent pas."
                                    }
                                } else {
                                    // Mise à jour uniquement des autres informations si les champs de mot de passe ne sont pas utilisés
                                    userRef.updateChildren(userMap as Map<String, Any>)
                                        .addOnSuccessListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Profil mis à jour avec succès.",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }.addOnFailureListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Erreur lors de la mise à jour du profil.",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                }

                                if (passwordChangeError.isNotEmpty()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            passwordChangeError,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Mettre à jour",
                                style = TextStyle(
                                    color = Color(0xffd08ae0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                            )
                        }

                        // Bouton pour sélectionner une nouvelle photo de profil
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Changer de photo de profil",
                                style = TextStyle(
                                    color = Color(0xffd08ae0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                            )
                        }
                        // Bouton de déconnexion
                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate("MainActivity") {
                                    popUpTo("ProfileEditScreen") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Déconnexion",
                                style = TextStyle(
                                    color = Color(0xffd08ae0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                            )
                        }

                        // Ajoutez un bouton pour supprimer le compte
                        Button(
                            onClick = { showDialog.value = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Supprimer le compte",
                                style = TextStyle(
                                    color = Color(0xffd08ae0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                            )
                        }

// Affichez un dialogue de confirmation pour la suppression du compte
                        if (showDialog.value) {
                            AlertDialog(
                                onDismissRequest = { showDialog.value = false },
                                title = { Text(text = "Confirmation") },
                                text = { Text(text = "Êtes-vous sûr de vouloir supprimer votre compte ?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            // Supprimez le compte de la base de données et de l'authentificateur
                                            userRef.removeValue().addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                                                        if (deleteTask.isSuccessful) {
                                                            // Redirigez l'utilisateur vers MainActivity ou une autre destination
                                                            navController.navigate("MainActivity") {
                                                                popUpTo("ProfileEditScreen") { inclusive = true }
                                                            }
                                                        } else {
                                                            // Gérez l'échec de la suppression du compte de l'authentificateur
                                                        }
                                                    }
                                                } else {
                                                    // Gérez l'échec de la suppression du compte de la base de données
                                                }
                                            }
                                        }
                                    ) {
                                        Text(text = "Confirmer")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDialog.value = false }
                                    ) {
                                        Text(text = "Annuler")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Fonction pour télécharger la nouvelle image dans Firebase Storage et mettre à jour l'URL dans la base de données
    fun uploadNewProfileImage(imageUri: Uri) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val userRef = db.getReference("Crushisen/user").child(userId)

        val storageRef = Firebase.storage.reference
        val imagesRef = storageRef.child("images_profile/${auth.currentUser?.uid}")

        imagesRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Téléchargement réussi, récupérez l'URL de téléchargement
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    // Mise à jour de l'URL de l'image dans la base de données en temps réel
                    userRef.child("photoUrl").setValue(uri.toString())
                        .addOnSuccessListener {
                            // Mise à jour réussie
                        }
                        .addOnFailureListener { exception ->
                            // Gestion des erreurs lors de la mise à jour de l'URL dans la base de données
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Gestion des erreurs lors du téléchargement de la nouvelle image dans Firebase Storage
            }
    }




    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun FeedEditScreen(navController: NavHostController) {
        val db = FirebaseDatabase.getInstance()
        val posts = remember { mutableStateListOf<Post>() }
        var isRefreshing by remember { mutableStateOf(false) }

        val postRef = db.getReference("Crushisen/posts").limitToLast(20)

        fun refreshPosts() {
            isRefreshing = true
            posts.clear()
            postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        posts.clear()

                        snapshot.children.forEach { postSnapshot ->
                            val ID_user = postSnapshot.child("ID_user").getValue(String::class.java) ?: ""
                            val description = postSnapshot.child("description").getValue(String::class.java) ?: ""
                            val likes = postSnapshot.child("like").getValue(Int::class.java) ?: 0
                            val photos = postSnapshot.child("photos").getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()

                            val userRef = db.getReference("Crushisen/user").child(ID_user)
                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val photoUrl = userSnapshot.child("photoUrl").getValue(String::class.java) ?: ""
                                    posts.add(Post(postSnapshot.key ?: "", ID_user, description, likes, photos, photoUrl))

                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e("Firebase", "Error fetching user photo URL: ${databaseError.message}")
                                }
                            })
                        }
                    }
                    isRefreshing = false
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching posts: ${databaseError.message}")
                    isRefreshing = false
                }
            })
            //dans la liste de post, si il y a plusieurs fois le meme id, on en garde qu'un

        }
        posts.sortBy { it.id }
        posts.reverse()
        //si posts contient 2 fois le meme post, on le supprime
        for (i in 0 until posts.size - 1) {
            if (posts[i].id == posts[i + 1].id) {
                posts.removeAt(i)
            }
        }


        // Appel initial pour charger les posts
        LaunchedEffect(Unit) { refreshPosts() }

        val gradientBackground = Brush.verticalGradient(
            colors = listOf(Color(0xfffd8487), Color(0xffb26ebe)),
            startY = 0f,
            endY = 700f
        )

        CrushIsenTheme {
            Scaffold(
                bottomBar = { BottomNavBar(navController = navController) },
                topBar = { FeedHeader(navController) },
                content = { paddingValues ->
                    Box(
                        modifier = Modifier
                            .background(brush = gradientBackground)
                            .fillMaxSize()
                    ) {
                        com.google.accompanist.swiperefresh.SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                            onRefresh = { refreshPosts() },
                        ) {
                            LazyColumn {
                                items(posts) { post ->
                                    Log.d("FeedEditScreen", "Post: $post")
                                    StyleCard(
                                        postId = post.id,
                                        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                        imageUris = post.photos,
                                        username = post.ID_user,
                                        description = post.description,
                                        initialLikesCount = post.likes,
                                        isInitiallyLiked = false,
                                        userProfileImageUrl = post.photoUrl
                                    )
                                }

                            }
                        }
                    }
                }
            )
        }
    }



    @OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
    @Composable
    fun StyleCard(
        postId: String, // Ajoutez l'ID du post
        userId: String, // Ajoutez l'ID de l'utilisateur actuel
        imageUris: List<String>,
        username: String,
        description: String,
        initialLikesCount: Int, // Ajoutez le nombre initial de likes
        isInitiallyLiked: Boolean,
        userProfileImageUrl: String // Ajoutez l'URL de la photo de profil de l'utilisateur ici

    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Crushisen/posts/$postId/likes")
        var liked by remember { mutableStateOf(isInitiallyLiked) }
        var likesCount by remember { mutableStateOf(initialLikesCount) }
        var showDialog by remember { mutableStateOf(false) }

        LaunchedEffect(postId) {
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    likesCount = dataSnapshot.childrenCount.toInt()
                    liked = dataSnapshot.hasChild(userId)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching post likes: ${databaseError.message}")
                }
            })
        }
        val pagerState = rememberPagerState(pageCount = { imageUris.size })

        Card(
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberImagePainter(data = userProfileImageUrl),
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(36.dp) // Increase the size here
                            .padding(8.dp)
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                var imageHeight by remember { mutableStateOf(200.dp) } // Default height

                val pagerState = rememberPagerState(pageCount = { imageUris.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .height(imageHeight.coerceAtMost(500.dp)) // Limit the maximum height
                        .clickable { showDialog = true }) { page ->
                    Image(
                        painter = rememberImagePainter(data = imageUris[page]),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .onSizeChanged { size ->
                                imageHeight = size.height.dp.coerceAtMost(500.dp) // Limit the maximum height
                            },
                        contentScale = ContentScale.Crop
                    )
                }


                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconToggleButton(
                        checked = liked,
                        onCheckedChange = { isLiked ->
                            liked = isLiked
                            if (isLiked) {
                                dbRef.child(userId)
                                    .setValue(System.currentTimeMillis()) // Use current time as value for sorting if needed
                            } else {
                                dbRef.child(userId).removeValue()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (liked) Color.Red else Color.Gray
                        )
                    }
                    Text(text = "$likesCount likes")
                }
            }
        }
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clickable { showDialog = false }) {
                    val pagerState = rememberPagerState(pageCount = { imageUris.size })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        Image(
                            painter = rememberImagePainter(data = imageUris[page]),
                            contentDescription = "Post Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}