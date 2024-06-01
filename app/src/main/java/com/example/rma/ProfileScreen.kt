import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserProfile(
    val email: String = "",
    val name: String = "",
    val picture: String = ""
)
@Composable
fun ProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val firestore = FirebaseFirestore.getInstance()

    var userProfile by remember { mutableStateOf(UserProfile()) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userProfile = document.toObject(UserProfile::class.java) ?: UserProfile()
                    }
                }
                .addOnFailureListener { exception ->
                    loadError = true
                    Log.e("ProfileScreen", "Error fetching profile data", exception)
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            if (userProfile.picture.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(userProfile.picture),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Email: ${userProfile.email}",
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Name: ${userProfile.name}",
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))


        Button(onClick = { navController.navigate("addnewlocation") },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF82CC1C)),
            modifier = Modifier.padding(end = 8.dp),
            shape = RoundedCornerShape(50)
        ) {
            androidx.compose.material.Text(
                "Dodaj novu lokaciju",
                color = Color.White,
                style = androidx.compose.material.MaterialTheme.typography.button,
            )
        }


        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF82CC1C)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                "Sign Out",
                color = Color.White,
                style = MaterialTheme.typography.button,
            )
        }

        if (loadError) {
            Text("Error loading profile", color = MaterialTheme.colors.error)
        }




    }

}

