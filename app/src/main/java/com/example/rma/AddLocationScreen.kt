package com.example.rma
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.time.Instant
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalViewConfiguration
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddNewLocationScreen(navController: NavController) {

    val currentUser = Firebase.auth.currentUser
    val db = Firebase.firestore
    val storage = Firebase.storage

    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }
    val outlinedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color(0xFF887177),
        unfocusedBorderColor = Color.Gray,
        cursorColor = Color(0xFF887177),
        focusedLabelColor = Color(0xFF887177)
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf8f7f7))
            .padding(16.dp)

    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedTextFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedTextFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedTextFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            colors = outlinedTextFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { pickImageLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF887177)),
            modifier = Modifier.padding(end = 8.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = "Choose Image",
                color = Color.White,
            )

        }

        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Selected Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier

                    .fillMaxWidth()
                    .padding(16.dp)

                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        Button(
            onClick = {
                Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()

                    addLocation(
                        context = context,
                        name,
                        latitude,
                        longitude,
                        description,
                        selectedImageUri,
                        currentUser?.uid,
                        storage,
                        db
                    )

                name = ""
                latitude = ""
                longitude = ""
                description = ""
                selectedImageUri = null
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF887177)),
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth()
                ,
            shape = RoundedCornerShape(50)
        ) {
            Text("Save Location", color = Color.White,)
        }

    }
}


private fun addLocation(
    context: Context,
    name: String,
    latitude: String,
    longitude: String,
    description: String,
    pictureUri: Uri?,
    userId: String?,
    storage: FirebaseStorage,
    db: FirebaseFirestore
) {

    val lat = latitude.toDoubleOrNull()
    val lng = longitude.toDoubleOrNull()

    if (name.isNotBlank() && lat != null && lng != null && description.isNotBlank() && userId != null) {
        db.collection("locations")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val id = querySnapshot.size() + 1

                val imageName = "$userId-${Instant.now()}-$id"
                val imageRef = storage.reference.child("uploadedLocationImages/$imageName")
                val uploadTask = imageRef.putFile(pictureUri!!)
                uploadTask.addOnSuccessListener { _ ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val location = hashMapOf(
                            "name" to name,
                            "latitude" to lat,
                            "longitude" to lng,
                            "description" to description,
                            "picture1" to imageUrl,
                            "addedByUser" to userId
                        )

                        db.collection("locations")
                            .add(location)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Location successfully uploaded!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to upload location.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
    } else {
        Toast.makeText(context, "Fill everything and add a picture!", Toast.LENGTH_SHORT).show()
    }
}
