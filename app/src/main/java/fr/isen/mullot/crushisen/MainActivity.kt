package fr.isen.mullot.crushisen

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme
import kotlin.math.round

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CrushIsenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginPage()
                }
            }
        }
    }
}

@Composable
fun LoginPage(viewModel: LoginViewModel = remember { LoginViewModel() }) {
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
                    modifier = Modifier
                        .padding(8.dp),
                    content = {
                        Text(
                            "Se connecter",
                            color = Color.White // Couleur du texte blanc
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Handle sign up button click */ },
                    modifier = Modifier
                        .padding(8.dp),// Couleur rose avec coins arrondis
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


class LoginViewModel : ViewModel() {
    // Add any necessary logic related to login or sign up here
}
