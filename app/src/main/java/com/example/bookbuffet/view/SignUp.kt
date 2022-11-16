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
fun SignUp(navController: NavHostController, context: ComponentActivity) {
    val auth = Firebase.auth
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmpassword by remember { mutableStateOf(TextFieldValue("")) }

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
                    .size(150.dp),
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
                    Text(
                        modifier = Modifier.padding(all = 8.dp),
                        text = "Sign Up",
                        style = MaterialTheme.typography.h1)
                    OutlinedTextField(
                        value = username,
                        label = { Text(text = "Username") },
                        onValueChange = { username = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally))
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
                    OutlinedTextField(
                        visualTransformation = PasswordVisualTransformation(),
                        value = confirmpassword,
                        label = { Text(text = "Confirm Password") },
                        onValueChange = { confirmpassword = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally))
                    Button(
                        onClick = {
                            if (username.text=="" || email.text=="" || password.text=="" || confirmpassword.text==""){
                                Toast.makeText(context, "Please fill in all the details!", Toast.LENGTH_SHORT).show()
                            }
                            else if (password.text != confirmpassword.text){
                                Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                auth.createUserWithEmailAndPassword(
                                    email.text,
                                    password.text
                                ).addOnCompleteListener(context) { task ->
                                    if (task.isSuccessful) {
                                        val currentUser = auth.currentUser
                                        val users = Users(
                                            null,
                                            currentUser!!.uid,
                                            username.text.trim(),
                                            email.text.trim(),
                                            password.text,
                                            "")
                                        val apiInterface = ApiInterface.create().postUser(users)
                                        apiInterface.enqueue(object: Callback<Users>{
                                            override fun onResponse(
                                                call: Call<Users>,
                                                response: Response<Users>
                                            ) {
                                                Log.d("Response", response.code().toString())
                                                Toast.makeText(context, "Register Successfully!", Toast.LENGTH_LONG).show()
                                                navController.navigate("signin") {
                                                    popUpTo("signin") {
                                                        inclusive = true
                                                    }
                                                }
                                            }

                                            override fun onFailure(call: Call<Users>, t: Throwable) {
                                                Toast.makeText(context, "Error has Occurred", Toast.LENGTH_LONG).show()
                                                Log.d("ERROR", t.toString())
                                            }

                                        })
                                    } else {
                                        Toast.makeText(context, "Firebase Error has Occurred", Toast.LENGTH_SHORT).show()
                                        Log.e("ERROR", task.exception.toString())
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Register")
                    }
                    TextButton(
                        onClick = {
                            navController.navigate("signin") {
                                popUpTo("signin") {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Already have an account? Login now!", style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}