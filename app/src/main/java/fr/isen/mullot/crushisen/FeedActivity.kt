package fr.isen.mullot.crushisen

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
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


@Composable
fun ProfileEditScreen(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseDatabase.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val userRef = db.getReference().child("Crushisen/user").child(userId)
    //val userRef = db.getReference().child("Crushisen/user").child("-NttfMPa_iu22Z85a5WZ") // Remplacer par l'ID utilisateur correct

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
        topBar = {
            TopAppBar(title = { Text("Edit Profile") })
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
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







@Composable
fun FeedEditScreen(navController: NavHostController){
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




/* package fr.isen.mullot.crushisen

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CrushIsenTheme {
                val viewModel = remember { MainViewModel() }
                val selectedItem = remember { mutableStateOf("Home") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(text = viewModel.message?.entries?.joinToString(", ") { "${it.key}: ${it.value}" } ?: "")
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = { viewModel.sendMessage("key2", "Hello, Firebase!") }) {
                            Text("Send Message")
                        }
                        BottomNavBar(selectedItem)
                    }

                    LaunchedEffect(true) {
                        viewModel.fetchData()
                    }
                }
            }
        }
    }
}

class MainViewModel : ViewModel() {
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

@Composable
fun GreetingPreview() {
    CrushIsenTheme {
        Text("Android")
    }
}
 */