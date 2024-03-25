package fr.isen.mullot.crushisen

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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

        // Initialiser Firebase
        FirebaseApp.initializeApp(this)
        setContent {
            CrushIsenTheme {
                // Initialiser le ViewModel avec le contexte de l'activité
                val viewModel = remember { MainViewModel() }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Afficher le message Firebase
                    Text(text = viewModel.message?.joinToString(", ") ?: "", modifier = Modifier.fillMaxSize())
                }

                // Lancer la récupération des données
                LaunchedEffect(true) {
                    viewModel.fetchData()
                }
            }
        }
    }
}class MainViewModel : ViewModel() {
    // Message to display
    var message by mutableStateOf<List<String>?>(null)
        private set // allows modification only within the class

    // Reference to the Firebase database
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // Fetch data from the Firebase database
    fun fetchData() {
        database.child("test").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val typeIndicator = object : GenericTypeIndicator<ArrayList<String>>() {}
                val value = dataSnapshot.getValue(typeIndicator)
                message = value ?: listOf("No message found")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching message: ${databaseError.message}")
                message = listOf("Error fetching message")
            }
        })
    }
}


@Composable
fun GreetingPreview() {
    CrushIsenTheme {
        Text("Android")
    }
}
