package com.example.bookbuffet.view

import android.app.ProgressDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bookbuffet.R
import com.example.bookbuffet.api.ApiInterface
import com.example.bookbuffet.model.Books
import com.example.bookbuffet.model.RentRequests
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun PendingRequests(
    navController: NavController,
    statusPending: String
) {
    val userId = Firebase.auth.currentUser!!.uid
    var rentsListRequesterId  = emptyList<RentRequests>()
    var rentsListReceiverId  = emptyList<RentRequests>()
    val apiInterface = ApiInterface.create().getRentRequestsByRequesterId(userId, statusPending).execute()
    if(apiInterface.isSuccessful) {
        rentsListRequesterId = apiInterface.body()!!
    }
    val apiInterface2 = ApiInterface.create().getRentRequestsByReceiverId(userId, statusPending).execute()
    if(apiInterface2.isSuccessful) {
        rentsListReceiverId = apiInterface2.body()!!
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = "Pending Requests")},
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
                        RentList(navController, rentsListRequesterId, rentsListReceiverId)
                    }

                }
            }


        }

    )
}

@Composable
fun RentList(
    navController: NavController,
    rentsListRequesterId: List<RentRequests>,
    rentsListReceiverId: List<RentRequests>
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        val itemsCount = rentsListRequesterId.size
        val itemsCount2 = rentsListReceiverId.size
        items(itemsCount) {
            RequesterRow(navController, it, rentsListRequesterId)
        }
        items(itemsCount2) {
            ReceiverRow(navController, it, rentsListReceiverId)
        }

    }
}

@Composable
fun RequesterRow(
    navController: NavController,
    rowIndex: Int,
    entries: List<RentRequests>
){
    RequesterEntry(navController = navController, entry = entries[rowIndex])
}

@Composable
fun RequesterEntry(
    navController: NavController,
    entry: RentRequests
){
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var title:String = ""
    var imageUrl:String = ""
    var ownerUsername:String = ""
    val apiInterface = ApiInterface.create().getBook(entry.bookId).execute()
    if(apiInterface.isSuccessful) {
        title = apiInterface.body()!!.title
        imageUrl = apiInterface.body()!!.imageUrl
    }
    val apiInterface2 = ApiInterface.create().getUserByUserId(entry.receiverId).execute()
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
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_request_quote_24),
                contentDescription = "Request Image",
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp),
                tint = Color.Black
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = "You have requested to rent a book from $ownerUsername",
                    style = MaterialTheme.typography.h1,
                    color = MaterialTheme.colors.onSurface,
                )
                Text(
                    text = "Book Title: $title",
                    style = MaterialTheme.typography.body2,
                )
                Text(
                    text = "Requested to Deliver On: ${entry.requestDate}",
                    style = MaterialTheme.typography.body2,
                )
                Button(
                    onClick = {
                        showDialog.value = true
                    }
                ){
                    Text(text = "Cancel Request")
                }
                if (showDialog.value) {
                    AlertDialog(
                        onDismissRequest = {
                            // Dismiss the dialog when the user clicks outside the dialog or on the back
                            // button. If you want to disable that functionality, simply use an empty
                            // onCloseRequest.
                            showDialog.value = false
                        },
                        title = {
                            Text(text = "Caution!!")
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to cancel this rent request? You can't undo this action!",
                                style = MaterialTheme.typography.h1
                            )
                        },
                        confirmButton = {
                            Button(

                                onClick = {
                                    showDialog.value = false
                                    val progressDialog = ProgressDialog(context)
                                    progressDialog.setMessage("Please wait...")
                                    progressDialog.setCancelable(false)
                                    progressDialog.show() //show progress dialog
                                    val rentRequests = RentRequests(
                                        entry.id,
                                        entry.requesterId,
                                        entry.receiverId,
                                        entry.bookId,
                                        entry.requestDate,
                                        "Cancel"
                                    )
                                    val apiInterface = ApiInterface.create().putRentRequest(rentRequests, entry.id!!)
                                    apiInterface.enqueue(object: Callback<RentRequests> {
                                        override fun onResponse(
                                            call: Call<RentRequests>,
                                            response: Response<RentRequests>
                                        ) {
                                            Log.d("Response", response.code().toString())
                                            if (progressDialog.isShowing) progressDialog.dismiss()
                                            Toast.makeText(context, "Request Cancelled Successfully!", Toast.LENGTH_LONG).show()
                                            navController.popBackStack()
                                            navController.navigateUp()


                                        }

                                        override fun onFailure(call: Call<RentRequests>, t: Throwable) {
                                            Toast.makeText(context, "Error has Occurred", Toast.LENGTH_LONG).show()
                                            Log.d("ERROR", t.toString())
                                        }

                                    })

                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                            ) {
                                Text("Yes", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(

                                onClick = {
                                    showDialog.value = false
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Text("No")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiverRow(
    navController: NavController,
    rowIndex: Int,
    entries: List<RentRequests>
){
    ReceiverEntry(navController = navController, entry = entries[rowIndex])
}

@Composable
fun ReceiverEntry(
    navController: NavController,
    entry: RentRequests
){
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
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
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_notification_important_24),
                contentDescription = "Request Image",
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp),
                tint = Color.Black
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = "$requesterUsername has requested to rent your book",
                    style = MaterialTheme.typography.h1,
                    color = MaterialTheme.colors.onSurface,
                )
                Text(
                    text = "Book Title: $title2",
                    style = MaterialTheme.typography.body2,
                )
                Text(
                    text = "Requested to Deliver On: ${entry.requestDate}",
                    style = MaterialTheme.typography.body2,
                )
                Button(
                    onClick = {
                        showDialog.value = true
                    }
                ){
                    Text(text = "Mark as Delivered")
                }
                if (showDialog.value) {
                    AlertDialog(
                        onDismissRequest = {
                            // Dismiss the dialog when the user clicks outside the dialog or on the back
                            // button. If you want to disable that functionality, simply use an empty
                            // onCloseRequest.
                            showDialog.value = false
                        },
                        title = {
                            Text(text = "Caution!!")
                        },
                        text = {
                            Text(
                                text = "Are you sure you have delivered the book to the requester before marking this request as 'Delivered'? You can't undo this action!",
                                style = MaterialTheme.typography.h1
                            )
                        },
                        confirmButton = {
                            Button(

                                onClick = {
                                    showDialog.value = false
                                    val progressDialog = ProgressDialog(context)
                                    progressDialog.setMessage("Please wait...")
                                    progressDialog.setCancelable(false)
                                    progressDialog.show() //show progress dialog
                                    val rentRequests = RentRequests(
                                        entry.id,
                                        entry.requesterId,
                                        entry.receiverId,
                                        entry.bookId,
                                        entry.requestDate,
                                        "Done"
                                    )
                                    val apiInterface = ApiInterface.create().putRentRequest(rentRequests, entry.id!!)
                                    apiInterface.enqueue(object: Callback<RentRequests> {
                                        override fun onResponse(
                                            call: Call<RentRequests>,
                                            response: Response<RentRequests>
                                        ) {
                                            Log.d("Response", response.code().toString())
                                            if (progressDialog.isShowing) progressDialog.dismiss()
                                            Toast.makeText(context, "Request Updated Successfully!", Toast.LENGTH_LONG).show()
                                            navController.popBackStack()
                                            navController.navigateUp()

                                        }

                                        override fun onFailure(call: Call<RentRequests>, t: Throwable) {
                                            Toast.makeText(context, "Error has Occurred", Toast.LENGTH_LONG).show()
                                            Log.d("ERROR", t.toString())
                                        }

                                    })

                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                            ) {
                                Text("Yes", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(

                                onClick = {
                                    showDialog.value = false
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Text("No")
                            }
                        }
                    )
                }
            }
        }
    }
}


