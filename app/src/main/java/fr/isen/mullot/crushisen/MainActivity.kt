package fr.isen.mullot.crushisen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.FirebaseApp


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
            CreateAccountPage()
        }
    }
}

fun navigateToCreateAccountScreen(context: Context, navController: NavController) {
    navController.navigate("createAccount")
}

@Composable
fun CreateAccountPage() {
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

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
    )  {
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
                value = "",
                onValueChange = {},
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Champ pour le prénom
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Prénom") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Champ pour l'adresse
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Adresse") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Champ pour la date de naissance
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Date de naissance") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Champ pour l'année à l'ISEN
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Année à l'ISEN") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Champ pour le numéro de téléphone
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Numéro de téléphone") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Champ pour la description
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Champ pour le pseudo
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Pseudo") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
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
                onClick = { /* Action lorsque le formulaire est soumis */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Valider")
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
                    modifier = Modifier.size(150.dp)
                )

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
                    onClick = { navController.navigate("createAccount") }, // Navigation vers la page de création de compte
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
