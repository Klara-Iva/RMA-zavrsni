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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.rma.ui.theme.RMATheme
import com.google.firebase.auth.FirebaseAuth


@Composable
fun LoginRegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val outlinedTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color(0xFF887177),
        unfocusedBorderColor = Color.Gray,
        cursorColor = Color(0xFF887177),
        focusedLabelColor = Color(0xFF887177)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf8f7f7))
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
        Button(onClick = {
            if(email.isNotEmpty() && password.isNotEmpty()){
                signIn(context, email, password,navController)}
            else{
                Toast.makeText(context, "Missing data", Toast.LENGTH_SHORT).show()
            }

        },


            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF887177),
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
                backgroundColor = Color(0xFF887177),
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
                (context as MainActivity).recreate()

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

