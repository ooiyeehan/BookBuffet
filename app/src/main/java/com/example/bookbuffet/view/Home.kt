package com.example.bookbuffet.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookbuffet.R
import com.example.bookbuffet.api.MainViewModel
import com.example.bookbuffet.model.BookCategories
import com.example.bookbuffet.model.Books
import com.example.bookbuffet.ui.theme.ButtonBlue
import com.example.bookbuffet.ui.theme.DarkerButtonBlue
import com.example.bookbuffet.ui.theme.Teal200
import com.example.bookbuffet.ui.theme.TextWhite
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



@Composable
fun HomeScreen(navController: NavController, booksList: List<Books>, bookCategoriesList: List<BookCategories>, viewModel: MainViewModel) {
    //yeehan1617@gmail.com
    //123456
    var selectedChipIndex by remember{
        mutableStateOf(0)
    }
    val userEmail = Firebase.auth.currentUser!!.email
    val selectedItem = remember { mutableStateOf("home")}
    val chips: MutableList<String> = mutableListOf()
    for (i in bookCategoriesList){
        chips.add(i.name)
    }

    Scaffold(
       topBar = {
           TopAppBar(
               title = {},
               navigationIcon = {
                   IconButton(onClick = {
                       navController.popBackStack()
                       navController.navigate("home") }
                   ) {
                       Icon(painterResource(id = R.drawable.ic_baseline_menu_book_24 ), contentDescription = null, tint = Color.Black)
                   }
               },
               actions = {
                   // RowScope here, so these icons will be placed horizontally
                   IconButton(onClick = { navController.navigate("searchScreen") }) {
                       Icon(Icons.Filled.Search, contentDescription = "Localized description")
                   }
                   IconButton(onClick = { navController.navigate("userDetails/$userEmail") }) {
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
                Column{
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    ){
                        Text(
                            text = "Books Available for Rent at Affordable Prices!",
                            style = MaterialTheme.typography.h1,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    LazyRow{
                        items(chips.size){
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(start = 15.dp, top = 15.dp, bottom = 15.dp)
                                    .clickable {
                                        selectedChipIndex = it


                                    }
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedChipIndex == it) ButtonBlue
                                        else DarkerButtonBlue
                                    )
                                    .padding(15.dp)
                            ){
                                Text(text = chips[it], color = TextWhite)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    BooksList(navController = navController, booksList = booksList)
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
                                Icon( painterResource(id = R.drawable.ic_baseline_local_library_24), contentDescription = "Library")
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

@Composable
fun BooksList(
    navController: NavController,
    booksList: List<Books>
) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        val itemCount = if(booksList.size % 2 == 0){
            booksList.size / 2
        } else{
            booksList.size / 2 + 1
        }
        items(itemCount){
            BookRow(rowIndex = it, entries = booksList, navController = navController )
        }
    }

}

@Composable
fun BookRow(
    rowIndex: Int,
    entries: List<Books>,
    navController: NavController
) {
    Column {
        Row {
            BookEntry(
                entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            if(entries.size >= rowIndex * 2 + 2){
                BookEntry(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            } else{
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BookEntry(
    entry: Books,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(Color.White)
            .clickable {
                navController.navigate("bookDetails/${entry.id}")
            }
    ){
        Column{
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = entry.title,
                modifier = Modifier
                    .size(80.dp)
                    .align(CenterHorizontally)
            )
            Text(
                text = entry.title,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}





