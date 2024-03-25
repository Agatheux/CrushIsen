package fr.isen.mullot.crushisen

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme

class FeedActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CrushIsenTheme {
                val viewModel = remember { FeedViewModel() }
                val selectedItem = remember { mutableStateOf("Home") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MainHeader(userName = "YourUserName") // Add your user name here
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