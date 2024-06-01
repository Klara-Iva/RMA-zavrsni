import android.annotation.SuppressLint
import android.content.Intent
import android.text.Layout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.rma.CameraBounds
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.rma.R

// Custom clickable modifier without ripple effect
@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
fun LocationDetailScreen(documentId: String, navController: NavController) {
    var location by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = Firebase.firestore

    // Animation state
    var animationScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = animationScale,
        animationSpec = tween(durationMillis = 100)
    )

    LaunchedEffect(documentId) {
        // Fetch location data
        FirebaseFirestore.getInstance().collection("locations")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    location = document.data
                }
            }

        // Check if the location is already a favorite
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
            kotlinx.coroutines.delay(100)
            animationScale = 1f
        }
    }

    location?.let { loc ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(loc["slika"] as String),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = loc["name"] as String,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = loc["opis"] as String,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Row for Button and Favorite Icon
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
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
                                navController.navigate("map")
                            }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF82CC1C)),
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    androidx.compose.material.Text(
                        "Prikazi na karti",
                        color = Color.White,
                        style = androidx.compose.material.MaterialTheme.typography.button,
                    )
                }

                Spacer(modifier = Modifier.width(100.dp))
                // Favorite Button with Animation and No Gray Overlay
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
