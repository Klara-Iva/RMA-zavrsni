package com.example.rma

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.rma.ui.theme.RMATheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginRegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val outlinedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color(0xFF82CC1C), // Color when focused
        unfocusedBorderColor = Color.Gray, // Color when not focused
        cursorColor = Color(0xFF82CC1C), // Color of the caret (pointer)
        focusedLabelColor = Color(0xFF82CC1C) // Color of the label when focused
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(

            text = "WELCOME",
            color = Color.Black,
            fontSize= 50.sp,
        )
        Spacer(modifier = Modifier.height(60.dp))
        OutlinedTextField(
            maxLines = 1,
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            colors = outlinedTextFieldColors
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            maxLines = 1,
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            colors = outlinedTextFieldColors

        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { signIn(context, email, password,navController) },colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF82CC1C),
        ),
            modifier = Modifier .padding(vertical = 12.dp, horizontal = 24.dp) ,
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = "Login",
                color = Color.White,
                style = MaterialTheme.typography.button,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
        Button(onClick = { navController.navigate("register") }
            , colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF82CC1C),
            ),
            shape = RoundedCornerShape(50)) {
                Text("Register"
                    ,color = Color.White)
        }
    }
}


private fun signIn(context: Context, email: String, password: String,navController: NavController) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("map")

            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
}




@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    RMATheme {
        LoginRegisterScreen(rememberNavController())
    }
}

