package com.example.rma

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import noRippleClickable

data class Location(
    val id: String,
    val name: String,
    val opis: String,
    val slika: String
)

@Composable
fun FavouritesScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = Firebase.firestore

    var favoriteLocations by remember { mutableStateOf<List<Location>>(emptyList()) }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val favorites = document.get("favourites") as? List<String> ?: emptyList()
                    fetchFavoriteLocations(favorites) { locations ->
                        favoriteLocations = locations
                    }
                }
        }
    }

    fun removeFromFavorites(locationId: String) {
        userId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val favorites = document.get("favourites") as? MutableList<String> ?: mutableListOf()
                    favorites.remove(locationId)
                    db.collection("users").document(uid).update("favourites", favorites)
                        .addOnSuccessListener {
                            // Refresh the favorite locations
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    val updatedFavorites = document.get("favourites") as? List<String> ?: emptyList()
                                    fetchFavoriteLocations(updatedFavorites) { locations ->
                                        favoriteLocations = locations
                                    }
                                }
                        }
                }
        }
    }

    Column(
        modifier = Modifier
            .background(Color(0xFFF0F0F0))
            .fillMaxSize()
            .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            color = Color.Black,
            text = "Favorite Locations",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)

        )
        if (favoriteLocations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No favorite locations found", textAlign = TextAlign.Center, color = Color.Black)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(favoriteLocations) { location ->
                    LocationItem(location = location, navController = navController, removeFromFavorites = { removeFromFavorites(location.id) })
                }
            }
        }
    }
}

fun fetchFavoriteLocations(favoriteIds: List<String>, callback: (List<Location>) -> Unit) {
    val db = Firebase.firestore
    val locations = mutableListOf<Location>()

    if (favoriteIds.isEmpty()) {
        callback(emptyList())
        return
    }

    favoriteIds.forEach { id ->
        db.collection("locations").document(id).get()
            .addOnSuccessListener { document ->
                document?.let {
                    val location = Location(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        opis = it.getString("opis") ?: "",
                        slika = it.getString("slika") ?: ""
                    )
                    locations.add(location)
                    if (locations.size == favoriteIds.size) {
                        callback(locations)
                    }
                }
            }
    }
}

@Composable
fun LocationItem(location: Location, navController: NavController, removeFromFavorites: () -> Unit) {
    Card(

        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable {
                removeFromFavorites()
            },
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(0.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(location.slika),
                contentDescription = location.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(0.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = location.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color.Black,
                    maxLines = 2
                )
                Text(
                    text = location.opis,
                    fontSize = 12.sp,
                    color = Color(0xFFFa1a1a1),
                    maxLines = 3,
                    lineHeight= 16.sp
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_favourite),
                contentDescription = "Favorite",
                modifier = Modifier
                    .absolutePadding(10.dp,0.dp,10.dp,0.dp)
                    .size(40.dp)
                    .noRippleClickable {
                        removeFromFavorites()
                    }
            )
        }
    }
}
