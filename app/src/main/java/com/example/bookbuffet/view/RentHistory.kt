package com.example.bookbuffet.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.bookbuffet.api.ApiInterface
import com.example.bookbuffet.model.RentRequests
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun RentHistory(
    navController: NavController,
    statusDone: String
) {
    val userId = Firebase.auth.currentUser!!.uid
    var rentsListRequesterId  = emptyList<RentRequests>()
    var rentsListReceiverId  = emptyList<RentRequests>()
    val apiInterface = ApiInterface.create().getRentRequestsByRequesterId(userId, statusDone).execute()
    if(apiInterface.isSuccessful) {
        rentsListRequesterId = apiInterface.body()!!
    }
    val apiInterface2 = ApiInterface.create().getRentRequestsByReceiverId(userId, statusDone).execute()
    if(apiInterface2.isSuccessful) {
        rentsListReceiverId = apiInterface2.body()!!
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Rent History") },
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
            ){
                RentListHistory(rentsListRequesterId, rentsListReceiverId)
            }

        }

    )
}

@Composable
fun RentListHistory(
    rentsListRequesterId: List<RentRequests>,
    rentsListReceiverId: List<RentRequests>
) {
   if(rentsListReceiverId.isEmpty() && rentsListRequesterId.isEmpty()) {
       Column(
           modifier = Modifier
               .fillMaxSize()
               .wrapContentSize(Alignment.Center)
               .background(Color.LightGray)
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
       Column(
           modifier = Modifier
               .fillMaxSize()
               .background(Color.LightGray)
       ){
           LazyColumn(
               modifier = Modifier.fillMaxWidth(),
               contentPadding = PaddingValues(16.dp)
           ) {
               val itemsCount = rentsListRequesterId.size
               val itemsCount2 = rentsListReceiverId.size

               items(itemsCount) {
                   RequesterRowHistory(it, rentsListRequesterId)
               }
               items(itemsCount2) {
                   ReceiverRowHistory(it, rentsListReceiverId)
               }


           }
       }
   }

}

@Composable
fun RequesterRowHistory(
    rowIndex: Int,
    entries: List<RentRequests>
){
    RequesterEntryHistory(entry = entries[rowIndex])
}

@Composable
fun RequesterEntryHistory(
    entry: RentRequests
){
    var title:String = ""
    var imageUrl:String = ""
    var ownerUserId: String = ""
    var ownerUsername: String = ""
    val apiInterface = ApiInterface.create().getBook(entry.bookId).execute()
    if(apiInterface.isSuccessful) {
        title = apiInterface.body()!!.title
        imageUrl = apiInterface.body()!!.imageUrl
        ownerUserId = apiInterface.body()!!.userId
    }
    val apiInterface2 = ApiInterface.create().getUserByUserId(ownerUserId).execute()
    if(apiInterface2.isSuccessful) {
        ownerUsername = apiInterface2.body()!!.username
    }
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        elevation = 5.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Book Image",
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp)
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = "You rented a book from '$ownerUsername' titled '$title'",
                    style = MaterialTheme.typography.h1,
                )
                Text(
                    text = "Date: ${entry.requestDate}",
                    style = MaterialTheme.typography.body2,
                )
            }
        }
    }
}

@Composable
fun ReceiverRowHistory(
    rowIndex: Int,
    entries: List<RentRequests>
){
    ReceiverEntryHistory(entry = entries[rowIndex])
}

@Composable
fun ReceiverEntryHistory(
    entry: RentRequests
){
    var title2:String = ""
    var imageUrl2:String = ""
    var requesterUsername: String = ""
    val apiInterface2 = ApiInterface.create().getBook(entry.bookId).execute()
    if(apiInterface2.isSuccessful) {
        title2 = apiInterface2.body()!!.title
        imageUrl2 = apiInterface2.body()!!.imageUrl
    }
    val apiInterface3 = ApiInterface.create().getUserByUserId(entry.requesterId).execute()
    if(apiInterface3.isSuccessful) {
        requesterUsername = apiInterface3.body()!!.username

    }
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        elevation = 5.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl2)
                    .crossfade(true)
                    .build(),
                contentDescription = "Book Image",
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp)
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = "Your book titled '$title2' was rented by $requesterUsername",
                    style = MaterialTheme.typography.h1,
                )
                Text(
                    text = "Date: ${entry.requestDate}",
                    style = MaterialTheme.typography.body2,
                )
            }
        }
    }
}