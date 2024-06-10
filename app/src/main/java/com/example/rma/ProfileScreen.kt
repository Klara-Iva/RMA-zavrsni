package com.example.rma
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage


data class UserProfile(
    val email: String = "",
    val name: String = "",
    val picture: String = ""
)


@Composable
fun ProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        val userId = user.uid
        val firestore = FirebaseFirestore.getInstance()
        val storage = Firebase.storage

        var userProfile by remember { mutableStateOf(UserProfile()) }
        var loadError by remember { mutableStateOf(false) }
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

        val pickImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedImageUri = it

            }

        }

        LaunchedEffect(userId) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userProfile =
                            document.toObject(UserProfile::class.java) ?: UserProfile()
                    }
                }
                .addOnFailureListener { exception ->
                    loadError = true
                    Log.e("ProfileScreen", "Error fetching profile data", exception)
                }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFf8f7f7))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            androidx.compose.material3.Text(
                color = Color.Black,
                text = "User Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)

                    .background(Color.White)


            ){
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.Center)

                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (userProfile.picture.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(userProfile.picture),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = " ${userProfile.name}",
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { pickImageLauncher.launch("image/*")


                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF887177)),
                modifier = Modifier

                    .padding(vertical = 12.dp, horizontal = 24.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    "Upload Profile Picture",
                    color = Color.White,
                    style = MaterialTheme.typography.button,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Email: ${userProfile.email}",
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))



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
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF887177)),
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

        if (selectedImageUri != null) {
            val context = LocalContext.current
            val imageName = "$userId.jpg"
            val imageRef = storage.reference.child("UserProfilePictures/$imageName")
            val uploadTask = imageRef.putFile(selectedImageUri!!)
            uploadTask.addOnSuccessListener { _ ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    firestore.collection("users").document(userId)
                        .update("picture", imageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProfileScreen", "Error updating profile picture URL", e)
                            Toast.makeText(context, "Failed to upload profile picture.", Toast.LENGTH_SHORT).show()
                        }
                }
            }

        }
    } else {
        navController.navigate("login")
    }
}

