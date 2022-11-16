package com.example.bookbuffet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bookbuffet.R
import com.example.bookbuffet.api.ApiInterface
import com.example.bookbuffet.model.Books
import com.example.bookbuffet.ui.theme.ButtonBlue
import com.example.bookbuffet.ui.theme.DarkerButtonBlue
import com.example.bookbuffet.ui.theme.Teal200
import com.example.bookbuffet.ui.theme.TextWhite

@Composable
fun SearchResult(
    navController: NavController,
    title: String
) {
    var booksList  = emptyList<Books>()
    val apiInterface = ApiInterface.create().getBooksBySearch(title).execute()
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
                            text = "You searched for: $title",
                            style = MaterialTheme.typography.h1,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if(booksList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        ){
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp)
                            ){
                                Text(
                                    text = "No Results found!",
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