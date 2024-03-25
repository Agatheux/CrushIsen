package fr.isen.mullot.crushisen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun BottomNavBar(selectedItem: MutableState<String>) {
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
                selected = selectedItem.value == "Home",
                onClick = { selectedItem.value = "Home" },
                icon = { Icon(painterResource(id = R.drawable.icon_home), contentDescription = null, Modifier.size(24.dp)) },
            )
            NavigationRailItem(
                selected = selectedItem.value == "Alert",
                onClick = { selectedItem.value = "Alert" },
                icon = { Icon(painterResource(id = R.drawable.icon_alert), contentDescription = null, Modifier.size(24.dp)) },
            )
            NavigationRailItem(
                selected = selectedItem.value == "Settings",
                onClick = { selectedItem.value = "Settings" },
                icon = { Icon(painterResource(id = R.drawable.icon_settings), contentDescription = null, Modifier.size(24.dp)) },
            )
        }
    }
}