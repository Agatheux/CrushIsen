package fr.isen.mullot.crushisen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.IconToggleButton
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme
import kotlinx.coroutines.launch


class FeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FeedActivityContent()
        }
    }
}

@Composable
fun FeedActivityContent() {

    val navController = rememberNavController()

    // Utiliser le NavController pour la navigation
    NavHost(navController = navController, startDestination = "feed") {
        composable("feed") {
            FeedEditScreen(navController)
        }
        composable("profile") {
            ProfileEditScreen(navController)

        }
        composable("notification") {
            NotificationEditScreen(navController)
        }

    }
}
@Composable
fun NotificationEditScreen(navController: NavHostController){

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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
    // État pour suivre l'élément sélectionné dans la BottomNavBar
    val selectedItem = remember { mutableStateOf("Feed") }

    // Écouteur de navigation pour changer l'élément sélectionné en fonction de la destination actuelle
    navController.addOnDestinationChangedListener { _, destination, _ ->
        selectedItem.value = when (destination.route) {
            "feed" -> "Feed"
            "profile" -> "Profile"
            "notification" -> "Notification"
            // Autres destinations à gérer si nécessaire
            else -> "Feed" // Définir une valeur par défaut
        }
    }

    // Navigation vers la destination "Feed" lorsque l'élément "Feed" est sélectionné
    fun navigateToFeed() {
        selectedItem.value = "Feed"
        navController.navigate("feed")
    }

    // Navigation vers la destination "Profile" lorsque l'élément "Profile" est sélectionné
    fun navigateToProfile() {
        selectedItem.value = "Profile"
        navController.navigate("profile")
    }

    // Navigation vers la destination "Notification" lorsque l'élément "Notification" est sélectionné
    fun navigateToNotification() {
        selectedItem.value = "Notification"
        navController.navigate("notification")
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
            // Élément de navigation pour "Feed"
            NavigationRailItem(
                selected = selectedItem.value == "Feed",
                onClick = { navigateToFeed() },
                icon = { Icon(painterResource(id = R.drawable.icon_home), contentDescription = null, Modifier.size(24.dp)) },
                // Autres paramètres de l'élément de navigation...
            )

            // Élément de navigation pour "Notification"
            NavigationRailItem(
                selected = selectedItem.value == "Notification",
                onClick = { navigateToNotification() },
                icon = { Icon(painterResource(id = R.drawable.icon_alert), contentDescription = null, Modifier.size(24.dp)) },
                // Autres paramètres de l'élément de navigation...
            )

            // Élément de navigation pour "Profile"
            NavigationRailItem(
                selected = selectedItem.value == "Profile",
                onClick = { navigateToProfile() },
                icon = { Icon(painterResource(id = R.drawable.icon_settings), contentDescription = null, Modifier.size(24.dp)) },
                // Autres paramètres de l'élément de navigation...
            )

            // Autres éléments de navigation si nécessaire
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profile") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TextFields pour pseudo, email, etc...
            OutlinedTextField(value = pseudo, onValueChange = { pseudo = it }, label = { Text("Pseudo") })
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            OutlinedTextField(value = annee_a_lisen, onValueChange = { annee_a_lisen = it }, label = { Text("Année a l'ISEN") })
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = { Text("Ancien mot de passe") },
                visualTransformation = PasswordVisualTransformation()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nouveau mot de passe") },
                visualTransformation = PasswordVisualTransformation()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmer le nouveau mot de passe") },
                visualTransformation = PasswordVisualTransformation()
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
                            val credential = EmailAuthProvider.getCredential(userEmail, oldPassword)
                            currentUser?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                                if (reauthTask.isSuccessful) {
                                    // Étape 2 : Mise à jour du mot de passe
                                    currentUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            // Réinitialisation des champs de mot de passe après mise à jour
                                            oldPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""

                                            // Mise à jour des autres informations de l'utilisateur
                                            userRef.updateChildren(userMap as Map<String, Any>).addOnSuccessListener {
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
                                            passwordChangeError = "Erreur lors de la mise à jour du mot de passe."
                                        }
                                    }
                                } else {
                                    passwordChangeError = "L'ancien mot de passe est incorrect."
                                }
                            }
                        } else {
                            passwordChangeError = "Les nouveaux mots de passe ne correspondent pas."
                        }
                    } else {
                        // Mise à jour uniquement des autres informations si les champs de mot de passe ne sont pas utilisés
                        userRef.updateChildren(userMap as Map<String, Any>).addOnSuccessListener {
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
                }
            ) {
                Text("Mettre à jour")
            }
        }

    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FeedEditScreen(navController: NavHostController) {
    CrushIsenTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(navController = navController)
            },
            topBar = {
                FeedHeader(userName = "Lost")
            },
            content = { paddingValues ->
                // Utiliser paddingValues pour respecter le padding requis par topBar et bottomBar
                androidx.compose.material.Surface(
                    Modifier
                        .padding(paddingValues) // Appliquer le padding global fourni par le Scaffold
                        .padding(horizontal = 8.dp) // Vous pouvez toujours ajouter vos propres paddings en plus
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF58529), // Orange
                                    Color(0xFFDD2A7B), // Pink
                                    Color(0xFF8134AF), // Purple
                                    Color(0xFF515BD4)  // Blue
                                )
                            )
                        ),
                ) {
                    StyleCard(
                        imageResList = listOf(
                            R.drawable.test_card,
                            R.drawable.test_card2,
                            R.drawable.test_card3
                        ),
                        username = "Crushisen" // Nom d'utilisateur à afficher
                    )
                }
            }
        )
    }




   @OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun StyleCard(
    imageResList: List<Int>, // Liste des ID de ressources d'images
    username: String, // Nom d'utilisateur à afficher
) {
    var liked by remember { mutableStateOf(false) } // État pour gérer si le post est aimé ou non
    var likesCount by remember { mutableStateOf(0) } // État pour gérer le nombre de likes
    var showDialog by remember { mutableStateOf(false) } // État pour gérer l'affichage de la Dialog

    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()) // Utilisez Column avec verticalScroll ici
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.icon_pp),
                    contentDescription = "User Icon",
                    modifier = Modifier
                        .size(36.dp) // Increase the size here
                        .padding(8.dp)
                )
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
            val pagerState = rememberPagerState(pageCount = { imageResList.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .height(100.dp) // Réduisez la hauteur ici
                    .clickable { showDialog = true }) { page ->
                Image(
                    painter = painterResource(id = imageResList[page]),
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(4.dp))
            Image(
                painter = painterResource(id = R.drawable.icon_comment),
                contentDescription = "Comment Icon",
                modifier = Modifier
                    .size(39.dp) // Set the size to be the same as the heart icon
                    .padding(8.dp)
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
                Text(text = "$likesCount") // Texte qui affiche le nombre de likes
                // Ajoutez plus d'éléments ici si nécessaire
            }
        }
    }

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





}
