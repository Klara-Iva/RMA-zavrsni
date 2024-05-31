import android.content.Intent
import android.text.Layout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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


@Composable
fun LocationDetailScreen(documentId: String,navController: NavController) {
    var location by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(documentId) {
        FirebaseFirestore.getInstance().collection("locations")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    location = document.data
                }
            }
    }

    location?.let { loc ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,

        ) {
            Image(
                painter = rememberAsyncImagePainter(loc["slika"] as String),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .padding(bottom = 0.dp)
            )
            Text(text = loc["name"] as String )
            Text(text = loc["opis"] as String)
            // Display other fields like image if needed

    Button(onClick = {

        var latitude : Double=0.0
        var longitude:Double=0.0
        val docRef = Firebase.firestore.collection("locations").document(documentId!!)
        docRef.get()
            .addOnSuccessListener { document ->
                latitude= document.data!!["latitude"].toString().toDouble()
                longitude= document.data!!["longitude"].toString().toDouble()
            }
            .addOnCompleteListener {
                CameraBounds.showSpecifiedLocationOnMap = true
                var camerapostion: CameraPosition = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude),19f)
                CameraBounds.camerapostion=camerapostion
                CameraBounds.showSpecifiedLocationOnMap = true
                CameraBounds.setCoordinates(latitude,longitude)
                navController.navigate("map")
            }

    },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF82CC1C),),
        modifier = Modifier .padding(vertical = 12.dp, horizontal = 24.dp) ,
        shape = RoundedCornerShape(50)

    ) {
        androidx.compose.material.Text("Prikazi na karti" ,
            color = Color.White,
            style = androidx.compose.material.MaterialTheme.typography.button,
           )
    }
}      }
}

