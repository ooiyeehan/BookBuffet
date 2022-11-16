package com.example.bookbuffet.view

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bookbuffet.R
import com.example.bookbuffet.api.ApiInterface
import com.example.bookbuffet.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@Composable
fun SignIn(navController: NavHostController, context: ComponentActivity) {
    val auth = Firebase.auth
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xfff1fbfb),
                        Color(0xfff5fbfc)
                    )
                ))
    ) {
        Column(
            modifier  = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .size(195.dp),
                painter = painterResource(id = R.drawable.ic_baseline_menu_book_24),
                contentDescription = "Logo",
                tint = Color.Black)
            Spacer(
                modifier = Modifier
                    .height(20.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                elevation = 10.dp
            ) {
                Column(
                ) {
                    OutlinedTextField(
                        value = email,
                        label = { Text(text = "Email") },
                        onValueChange = { email = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally))
                    OutlinedTextField(
                        visualTransformation = PasswordVisualTransformation(),
                        value = password,
                        label = { Text(text = "Password") },
                        onValueChange = { password = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally))
                    Button(
                        onClick = {
                            if (email.text=="" || password.text==""){
                                Toast.makeText(context, "Please enter your details!", Toast.LENGTH_SHORT).show()
                            } else {
                                auth.signInWithEmailAndPassword(
                                    email.text,
                                    password.text
                                ).addOnCompleteListener(context) { task ->
                                    if (task.isSuccessful){
                                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                        navController.navigate("home") {
                                            popUpTo("home") {
                                                inclusive = true
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Incorrect email or password!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Login")
                    }
                    TextButton(
                        onClick = {
                            navController.navigate("signup") {
                                popUpTo("signup") {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Don't have an account? Sign up now!", style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}