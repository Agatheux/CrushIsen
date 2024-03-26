package fr.isen.mullot.crushisen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPagerIndicator
import fr.isen.mullot.crushisen.ui.theme.CrushIsenTheme

@OptIn(ExperimentalPagerApi::class)
class CardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrushIsenTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    StyleCard(
                        imageResList = listOf(R.drawable.test_card, R.drawable.test_card2, R.drawable.test_card3), // Remplacez 'test_card1', 'test_card2', 'test_card3' par vos ressources images
                        initialComment = "Commentaire initial à afficher"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun StyleCard(
    imageResList: List<Int>, // Liste des ID de ressources d'images
    initialComment: String, // Commentaire initial à afficher
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
        Column {
            val pagerState = rememberPagerState(pageCount = { imageResList.size })
            HorizontalPager(state = pagerState, modifier = Modifier.height(200.dp).clickable { showDialog = true }) { page ->
                Image(
                    painter = painterResource(id = imageResList[page]),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = initialComment,
                style = MaterialTheme.typography.body1,
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
                Text(text = "$likesCount") // Texte qui affiche le nombre de likes
                // Ajoutez plus d'éléments ici si nécessaire
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(modifier = Modifier.fillMaxSize().clickable { showDialog = false }) {
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
@Preview(showBackground = true)
@Composable
fun PreviewStyleCard() {
    CrushIsenTheme {
        StyleCard(
            imageResList = listOf(R.drawable.test_card, R.drawable.test_card2, R.drawable.test_card3), // Remplacez 'test_card1', 'test_card2', 'test_card3' par vos ressources images
            initialComment = "test test test tessstttt"
        )
    }
}