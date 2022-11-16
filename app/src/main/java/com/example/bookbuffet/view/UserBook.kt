package com.example.bookbuffet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bookbuffet.api.ApiInterface
import com.example.bookbuffet.model.Books

@Composable
fun UserBooks(
    navController: NavController,
    userId: String
) {
    var booksList  = emptyList<Books>()
    val apiInterface = ApiInterface.create().getBooksByUserId(userId).execute()
    if(apiInterface.isSuccessful) {
        booksList = apiInterface.body()!!
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.Black)
                    }
                },
                actions = {
                    // RowScope here, so these icons will be placed horizontally
                }
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ) {
                Column{
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ){
                        Text(
                            text = "Your Books",
                            style = MaterialTheme.typography.h1,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if(booksList.isEmpty()) {
                        Column{
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp)
                            ){
                                Text(
                                    text = "You don't have any books uploaded!",
                                    style = MaterialTheme.typography.h1,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                    } else {
                        BooksList(navController = navController, booksList = booksList)
                    }

                }

            }
        }

    )
}