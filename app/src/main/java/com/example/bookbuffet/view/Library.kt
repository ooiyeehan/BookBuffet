package com.example.bookbuffet.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bookbuffet.R
import com.example.bookbuffet.ui.theme.Teal200
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun LibraryScreen(navController: NavController) {
    val selectedItem = remember { mutableStateOf("library") }
    val context = LocalContext.current
    val statusDone = "Done"
    val statusPending = "Pending"
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        navController.navigate("home") }) {
                        Icon(painterResource(id = R.drawable.ic_baseline_menu_book_24 ), contentDescription = null, tint = Color.Black)
                    }
                },
                actions = {
                    // RowScope here, so these icons will be placed horizontally
                    IconButton(onClick = { navController.navigate("searchScreen") }) {
                        Icon(Icons.Filled.Search, contentDescription = "Localized description")
                    }
                    IconButton(onClick = { navController.navigate("userDetails/${Firebase.auth.currentUser!!.email}") }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Localized description")
                    }
                }
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ){
                        Text(
                            text = "Welcome to Book Buffet! Here is your personal space",
                            style = MaterialTheme.typography.h1,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Button(
                            onClick = {
                            /* RentHistory */
                                navController.navigate("rentHistory/$statusDone")
                            },
                            // Uses ButtonDefaults.ContentPadding by default
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                top = 12.dp,
                                end = 20.dp,
                                bottom = 12.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // Inner content including an icon and a text label
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_history_24),
                                contentDescription = "Edit",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Rent History")
                        }

                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("userBooks/${Firebase.auth.currentUser!!.uid}") },
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                top = 12.dp,
                                end = 20.dp,
                                bottom = 12.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // Inner content including an icon and a text label
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_collections_bookmark_24),
                                contentDescription = "Edit",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Your Books")
                        }

                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Button(
                            onClick = {
                            /* Pending Rent */
                                navController.navigate("pendingRequests/$statusPending")
                            },
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                top = 12.dp,
                                end = 20.dp,
                                bottom = 12.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // Inner content including an icon and a text label
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_pending_24),
                                contentDescription = "Edit",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Your Pending Requests")
                        }

                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ) {
                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                Toast.makeText(context, "Logout Successful!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                                navController.navigate("signin"){
                                    popUpTo("signin") {
                                        inclusive = true
                                    }
                                } },
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                top = 12.dp,
                                end = 20.dp,
                                bottom = 12.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            // Inner content including an icon and a text label
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_login_24),
                                contentDescription = "Edit",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Logout")
                        }

                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }


            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("create")
                },
                shape = RoundedCornerShape(50),
                backgroundColor = Teal200
            ) {
                Icon(Icons.Filled.Add, tint = Color.White, contentDescription = "Add")
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,

        bottomBar = {
            BottomAppBar(
                cutoutShape = RoundedCornerShape(50),
                content = {
                    BottomNavigation {
                        BottomNavigationItem(
                            selected = selectedItem.value == "home",
                            onClick = {
                                navController.navigate("home")
                                selectedItem.value = "home"
                            },
                            icon = {
                                Icon(Icons.Default.Home, contentDescription = "Home")
                            },
                            label = {
                                Text(text = "Home")
                            }
                        )
                        BottomNavigationItem(
                            selected = selectedItem.value == "library",
                            onClick = {
                                navController.navigate("library")
                                selectedItem.value = "library"
                            },
                            icon = {
                                Icon(painterResource(id = R.drawable.ic_baseline_local_library_24), contentDescription = "Library")
                            },
                            label = {
                                Text(text = "Library")
                            }
                        )
                    }
                }
            )
        }
    )
}