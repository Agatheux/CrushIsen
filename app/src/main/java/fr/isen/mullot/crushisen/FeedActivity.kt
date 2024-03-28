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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme
import kotlinx.coroutines.launch

data class Post(
    val ID_user: String,
    val description: String,
    val like: Int,
    val photos: List<String>
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
    // Génère un ID unique pour le post
    val postId = FirebaseDatabase.getInstance().getReference("Crushisen").child("post").push().key ?: return

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
                        .child("post")
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.run {
                padding(padding)
                    .padding(16.dp)
            }) {
                OutlinedTextField(
                    value = description.value,
                    onValueChange = { description.value = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose Image")
                }
                Spacer(modifier = Modifier.height(16.dp))
              Button(
    onClick = { imageUris = emptyList() }, // Set imageUris to a new empty list
    modifier = Modifier.fillMaxWidth()
) {
    Text("Remove Images")
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
                            Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
                            onBack() // Navigate back after posting
                            onBack() // Navigate back after posting
                        } else {
                            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Post")
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
                selected = selectedItem.value == "Profile",
                onClick = { navigateToProfile() },
                icon = { Icon(painterResource(id = R.drawable.icon_settings), contentDescription = null, Modifier.size(24.dp)) },
            )

            NavigationRailItem(
                selected = selectedItem.value == "CreatePost",
                onClick = { navigateToCreatePost() },
                icon = { Icon(painterResource(id = R.drawable.icon_post), contentDescription = null, Modifier.size(24.dp)) },
            )
        }
    }
}

    @Composable
    fun FeedHeader(userName: String) {
        val imagePainter = painterResource(id = R.drawable.icon_pp)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = imagePainter,
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(80.dp, 80.dp) // Set the size here
                    .padding(start = 16.dp) // Add padding to the start
            )
            Text(
                text = userName,
                modifier = Modifier.padding(start = 16.dp),
                fontSize = 20.sp, // Increase
                fontWeight = FontWeight.Bold// the font size here
            )
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

        LaunchedEffect(key1 = userId) {
            if (userId.isNotEmpty()) {
                userRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData = snapshot.value as? Map<*, *>
                        userData?.let {
                            pseudo = it["pseudo"].toString()
                            email = it["email"].toString()
                            description = it["description"].toString()
                            phone = it["numero"].toString()
                            annee_a_lisen = it["annee_a_lisen"].toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ProfileEditScreen", "Failed to load user data: $error")
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Failed to load user data.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                })
            }
        }

        val gradientBackground = Brush.linearGradient(
            colors = listOf(Color(	0xfffd8487), Color(0xffb26ebe)),
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
            )
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                OutlinedTextField(
                    value = pseudo,
                    onValueChange = { pseudo = it },
                    label = {Text(text = "Pseudo", color=Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp)) // Ajouter un espace
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text="Email", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp)) // Ajouter un espace
                // Ajoutez des Spacers et Modifier.fillMaxWidth() aux autres OutlinedTextField de la même manière
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text ="Description", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp)) // Ajouter un espace
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(text="Phone", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp)) // Ajouter un espace
                OutlinedTextField(
                    value = annee_a_lisen,
                    onValueChange = { annee_a_lisen = it },
                    label = { Text(text = "Année a l'ISEN", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp)) // Ajouter un espace
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text(text = "Ancien mot de passe", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp)) // Ajouter un espace
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(text = "Nouveau mot de passe", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp)) // Ajouter un espace
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(text = "Confirmer le nouveau mot de passe", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.White, // Couleur du contour lorsque le champ n'est pas sélectionné
                        focusedBorderColor = Color.White // Couleur du contour lorsque le champ est sélectionné
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (passwordChangeError.isNotEmpty()) {
                    Text(passwordChangeError, color = Color.Red)
                }




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
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White), // Couleur de fond blanche
            shape = RoundedCornerShape(20.dp)
                ) {
            Text(
                text = "Mettre a jour",
                style = TextStyle(
                    color = Color(0xffd08ae0),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,

                    )
            )
                }
            }

        }
    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun FeedEditScreen(navController: NavHostController) {
        val db = FirebaseDatabase.getInstance()
        val posts = remember { mutableStateListOf<Post>() }

        val postRef = db.getReference("Crushisen/post").limitToFirst(10)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach { postSnapshot ->
                        val postData = postSnapshot.value as? Map<*, *>
                        postData?.let {
                            val ID_user = it["ID_user"].toString()
                            val description = it["description"].toString()
                            val like = it["like"]?.toString()?.toIntOrNull() ?: 0
                            val photos = it["photos"] as? List<String> ?: listOf()

                            // Ajoutez chaque post à la liste des posts
                            posts.add(Post(ID_user, description, like, photos))
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching posts: ${databaseError.message}")
            }
        })


        CrushIsenTheme {
            Scaffold(
                bottomBar = {
                    BottomNavBar(navController = navController)
                },
                topBar = {
                    FeedHeader(userName = "Lost")
                },
                content = { paddingValues ->
                    Surface(
                        Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 8.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFF58529),
                                        Color(0xFFDD2A7B),
                                        Color(0xFF8134AF),
                                        Color(0xFF515BD4)
                                    )
                                )
                            ),
                    ) {
                        LazyColumn {
                            items(posts.size) { index ->
                                val post = posts[index]
                                Log.d("FeedEditScreen", "Post: $post")
                                StyleCard(
                                    imageUris = post.photos,// Passer la liste des URL d'images
                                    username = post.ID_user,
                                    description = post.description
                                )
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
        imageUris: List<String>, // La liste des URLs d'images à afficher
        username: String, // Nom d'utilisateur à afficher
        description: String, // Description à afficher
    ) {
        var liked by remember { mutableStateOf(false) }
        var likesCount by remember { mutableStateOf(0) }
        var showDialog by remember { mutableStateOf(false) }

        Card(
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_pp),
                        contentDescription = "User Icon",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .padding(8.dp)
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                imageUris.forEach { imageUrl ->
                    Image(
                        painter = rememberImagePainter(data = imageUrl),
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .height(200.dp) // Set a fixed height or use .fillMaxWidth() for dynamic size
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)), // Add some rounded corners
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp)) // Add space between images if multiple
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
                        onCheckedChange = {
                            liked = it
                            if (it) likesCount++ else if (likesCount > 0) likesCount--
                        }
                    ) {
                        Icon(
                            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (liked) Color.Red else Color.Gray
                        )
                    }
                    Text(text = "$likesCount likes") // Display the likes count
                }
            }
        }
    }
}



/*
if (showDialog) {
    Dialog(onDismissRequest = { showDialog = false }) {
        Box(modifier = Modifier
            .fillMaxSize()
            .clickable { showDialog = false }) {
            val pagerState = rememberPagerState(pageCount = { imageResList.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                Image(
                    painter = painterResource(id = imageResList[page]),
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
}

 */
