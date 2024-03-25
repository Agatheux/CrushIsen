package fr.isen.mullot.crushisen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme

class HeaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrushIsenTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        MainHeader(userName = "YourUserName") // Add your user name here
                        Greeting("Android")
                    }
                }
            }
        }
    }
}

@Composable
fun MainHeader(userName: String) {
    val imagePainter = painterResource(id = R.drawable.icon_pp)
    Image(
        painter = imagePainter,
        contentDescription = "Profile Photo",
        modifier = Modifier.size(100.dp)
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    CrushIsenTheme {
        Column {
            MainHeader(userName = "YourUserName") // Add your user name here
            Greeting("Android")
        }
    }
}