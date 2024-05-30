import android.text.Layout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore



@Composable
fun LocationDetailScreen(documentId: String) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,

        ) {
            Text(text = loc["name"] as String )
            Text(text = loc["opis"] as String)
            // Display other fields like image if needed
        }
    }
}
