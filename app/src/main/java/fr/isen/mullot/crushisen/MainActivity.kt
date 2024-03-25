package fr.isen.mullot.crushisen

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
