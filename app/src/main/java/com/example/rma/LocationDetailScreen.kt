package com.example.rma
import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LocationDetailScreen(documentId: String, navController: NavController) {
    var location by remember { mutableStateOf<Map<String, Any>?>(null) }
    var images by remember { mutableStateOf(listOf<String>()) }
    var isFavorite by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = Firebase.firestore
    var animationScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = animationScale,
        animationSpec = tween(durationMillis = 100)
    )

    LaunchedEffect(documentId) {

        FirebaseFirestore.getInstance().collection("locations")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    location = document.data
                    val imageUrls = mutableListOf<String>()
                    for (i in 1..5) {
                        document.getString("picture$i")?.let { imageUrls.add(it) }
                    }
                    images = imageUrls
                }
            }
        userId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val favorites = document.get("favourites") as? List<String> ?: emptyList()
                    isFavorite = favorites.contains(documentId)
                }
        }
    }

    fun toggleFavorite() {
        userId?.let { uid ->
            val userRef = db.collection("users").document(uid)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val favorites = snapshot.get("favourites") as? MutableList<String> ?: mutableListOf()
                if (isFavorite) {
                    favorites.remove(documentId)
                } else {
                    favorites.add(documentId)
                }
                transaction.update(userRef, "favourites", favorites)
            }.addOnSuccessListener {
                isFavorite = !isFavorite
                // Trigger animation
                animationScale = 1.2f
            }
        }
    }

    LaunchedEffect(animationScale) {
        if (animationScale > 1f) {
            delay(100)
            animationScale = 1f
        }
    }

    location?.let { loc ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFf8f7f7)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                if (images.isNotEmpty()) {
                    val pagerState = rememberPagerState()
                    HorizontalPager(
                        count = images.size,
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) { page ->
                        Image(
                            painter = rememberAsyncImagePainter(images[page]),
                            contentDescription = "Location Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(images.size) { index ->
                            val color = if (pagerState.currentPage == index) Color.Black else Color.LightGray
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .padding(4.dp)
                            )
                            if (index < images.size - 1) {
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            item {
                Text(
                    text = loc["name"] as String,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = loc["description"] as String,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(25.dp),
                    textAlign = TextAlign.Justify,
                    color = Color(0xFFFa1a1a1),
                    lineHeight = 20.sp
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            var latitude: Double = 0.0
                            var longitude: Double = 0.0
                            val docRef = db.collection("locations").document(documentId)
                            docRef.get()
                                .addOnSuccessListener { document ->
                                    latitude = document.data!!["latitude"].toString().toDouble()
                                    longitude = document.data!!["longitude"].toString().toDouble()
                                }
                                .addOnCompleteListener {
                                    CameraBounds.showSpecifiedLocationOnMap = true
                                    val cameraPosition = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 19f)
                                    CameraBounds.camerapostion = cameraPosition
                                    CameraBounds.showSpecifiedLocationOnMap = true
                                    CameraBounds.setCoordinates(latitude, longitude)
                                    navController.navigate("map") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF887177)),
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        androidx.compose.material.Text(
                            "Find on map",
                            color = Color.White,
                            style = androidx.compose.material.MaterialTheme.typography.button,
                        )
                    }
                    Spacer(modifier = Modifier.width(100.dp))

                    Image(
                        painter = painterResource(id = if (isFavorite) R.drawable.ic_favourite else R.drawable.ic_favourite_border),
                        contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                        modifier = Modifier
                            .size(55.dp)
                            .scale(animatedScale)
                            .noRippleClickable { toggleFavorite() }
                    )
                }
            }
        }
    }
}
