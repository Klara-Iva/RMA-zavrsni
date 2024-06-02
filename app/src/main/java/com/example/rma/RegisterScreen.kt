import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.rma.MainActivity
import com.example.rma.ui.theme.RMATheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val outlinedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color(0xFF82CC1C), // Color when focused
        unfocusedBorderColor = Color.Gray, // Color when not focused
        cursorColor = Color(0xFF82CC1C), // Color of the caret (pointer)
        focusedLabelColor = Color(0xFF82CC1C) // Color of the label when focused
    )

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            maxLines = 1,
            text = "REGISTER NOW!",
            color = Color.Black,
            fontSize= 30.sp,
        )
        Spacer(modifier = Modifier.height(60.dp))


        OutlinedTextField(
            maxLines = 1,
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            colors = outlinedTextFieldColors,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),

        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            maxLines = 1,
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            colors = outlinedTextFieldColors,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),

        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = outlinedTextFieldColors,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),

        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
               if(name.isNotEmpty()&&email.isNotEmpty()&&password.isNotEmpty()){
                register(context, name, email, password,navController)}
               else{
                   Toast.makeText(context, "Missing data", Toast.LENGTH_SHORT).show()
               }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF82CC1C),
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 24.dp)
        ) {
            Text(
                "Register",
                color = Color.White,
                style = MaterialTheme.typography.button,
                fontSize = 20.sp
            )
        }



        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF82CC1C),
            ),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = "Back to Login",
                color = Color.White
            )
        }
    }


}

private fun register(context: Context, name: String, email: String, password: String,navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // User is successfully registered, now save to Firestore
                val user = hashMapOf(
                    "name" to name,
                    "email" to email
                )

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    db.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show()
                            (context as MainActivity).recreate()//????
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RMATheme {
        RegisterScreen(rememberNavController())
    }
}
