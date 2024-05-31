import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var locations by remember { mutableStateOf(listOf<Location>()) }
    var allLocations by remember { mutableStateOf(listOf<Location>()) }


        // Fetch locations on initial load
        LaunchedEffect(Unit) {
            fetchLocationsFromFirestore { result ->
                allLocations = result
                locations = result
            }
        }

        Column(  modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)) // Add background color here
            .padding(16.dp)) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    locations = if (searchQuery.isEmpty()) {
                        allLocations
                    } else {
                        allLocations.filter { location ->
                            location.name.contains(searchQuery, ignoreCase = true)
                        }
                    }
                },
                label = { Text("Search Locations") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // Perform filtering when search button is clicked
                    locations = allLocations.filter { location ->
                        location.name.contains(searchQuery, ignoreCase = true)
                    }
                },
                modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF82CC1C),
            ),
            shape = RoundedCornerShape(50)
            ) {
                Text("Search",
                color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Display search results
            LazyColumn {
                items(locations) { location ->
                    LocationCard(location, navController)
                }
            }
        }

}
data class Location(val id: String, val name: String, val imageUrl: String, val description: String)

@Composable
fun LocationCard(location: Location, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("locationDetail/${location.id}")
            }
    ) {
        Row {
            Image(
                painter = rememberImagePainter(data = location.imageUrl),
                contentScale = ContentScale.Crop,
                contentDescription = location.name,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.padding(8.dp)) {
                Text(location.name, style = MaterialTheme.typography.h6)
                Text(location.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}


fun fetchLocationsFromFirestore(onResult: (List<Location>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("locations")
        .get()
        .addOnSuccessListener { documents ->
            val locations = documents.mapNotNull { doc ->
                val name = doc.getString("name")
                val imageUrl = doc.getString("slika")
                val description = doc.getString("opis")
                if (name != null && imageUrl != null && description != null) {
                    Location(doc.id, name, imageUrl, description)
                } else {
                    null
                }
            }
            onResult(locations)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}



