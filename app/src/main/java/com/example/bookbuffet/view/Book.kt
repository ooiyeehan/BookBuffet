package com.example.bookbuffet.view

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.bookbuffet.R
import com.example.bookbuffet.api.ApiInterface
import com.example.bookbuffet.model.BookCategories
import com.example.bookbuffet.model.Books
import com.example.bookbuffet.model.RentRequests
import com.example.bookbuffet.model.Users
import com.example.bookbuffet.ui.theme.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddBookScreen(navController: NavController, bookCategoriesList: List<BookCategories>) {
    //Declare variables for database insert
    val userId = Firebase.auth.currentUser!!.uid
    Log.e("FIREBASEUSER", "$userId")
    var title by remember { mutableStateOf(TextFieldValue(""))}
    var description by remember { mutableStateOf(TextFieldValue(""))}
    var genre by remember { mutableStateOf("") }
    var rentFee by remember { mutableStateOf(TextFieldValue(""))}
    var downloadUrl: String

    val suggestions : MutableList<String> = mutableListOf()
    for (i in bookCategoriesList){
        suggestions.add(i.name)
    }
    val bgColor = Color.White
    suggestions.removeAt(0)

    //Setting up DropdownMenu
    var expanded by remember { mutableStateOf(false) }
    var textfieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    //Setting up ImagePicker from Gallery
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    var isVisible: Boolean
    if(imageUri == null){
        isVisible = true
    }
    else{
        isVisible = false
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = "Add Book")},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.Black)
                    }
                },
                actions = {
                    // RowScope here, so these icons will be placed horizontally
                    Button(
                        onClick = {
                            if(imageUri == null){ //If Image is not selected
                                Toast.makeText(context, "Please upload an image!", Toast.LENGTH_LONG).show()
                            }

                            else{ //If any fields are empty
                                if(title.text == "" || genre == "" || description.text == "" || rentFee.text == "" ){
                                    Toast.makeText(context, "Please insert all the details!", Toast.LENGTH_LONG).show()
                                    Log.e("Data", "${title.text}\n ${genre}\n" +
                                            " ${description.text}\n" +
                                            " ${rentFee.text} ")
                                }

                                else{ //All are filled with user data
                                    val progressDialog = ProgressDialog(context)
                                    progressDialog.setMessage("Please wait...")
                                    progressDialog.setCancelable(false)
                                    progressDialog.show() //show progress dialog

                                    //format image file name
                                    val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
                                    val now = Date()
                                    val fileName = formatter.format(now)
                                    //insert into firebase storage
                                    val storageReference = FirebaseStorage.getInstance().getReference()
                                    val ref = storageReference.child("images/$fileName")
                                    val uploadTask = ref.putFile(imageUri!!)
                                    //get url of image uploaded in firebase storage
                                    val urlTask = uploadTask.continueWithTask { task ->
                                        if (!task.isSuccessful) {
                                            task.exception?.let {
                                                throw it
                                            }
                                        }
                                        ref.downloadUrl
                                    }.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val downloadUri = task.result
                                            Log.e(
                                                "Firebase Image URL",
                                                downloadUri.toString()
                                            )
                                            val books = Books(
                                                null,
                                                userId,
                                                title.text.trim(),
                                                description.text.trim(),
                                                downloadUri.toString(),
                                                genre,
                                                rentFee.text.toDouble())
                                            //call post book api
                                            val apiInterface = ApiInterface.create().postBook(books)
                                            apiInterface.enqueue(object: Callback<Books> {
                                                override fun onResponse(
                                                    call: Call<Books>,
                                                    response: Response<Books>
                                                ) {
                                                    Log.d("Response", response.code().toString())
                                                    if (progressDialog.isShowing) progressDialog.dismiss()
                                                    Toast.makeText(context, "Book Added Successfully!", Toast.LENGTH_LONG).show()
                                                    navController.popBackStack()
                                                    navController.navigate("home") {
                                                        popUpTo("home") {
                                                            inclusive = true
                                                        }
                                                    }
                                                }

                                                override fun onFailure(call: Call<Books>, t: Throwable) {
                                                    Toast.makeText(context, "Error has Occurred", Toast.LENGTH_LONG).show()
                                                    Log.d("ERROR", t.toString())
                                                }

                                            })
                                        } else {
                                            // Handle failures
                                            Toast.makeText(context, "Firebase Error in Obtaining Image URL", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                }

                            }
                        },
                        // Uses ButtonDefaults.ContentPadding by default
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 12.dp,
                            end = 20.dp,
                            bottom = 12.dp
                        ),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Teal200)
                    ) {
                        // Inner content including an icon and a text label
                        Icon(
                            Icons.Filled.AddCircle,
                            contentDescription = "Add",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "Add")
                    }

                }
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
//                    .scrollable(ScrollableState {  })
            ){
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.TopCenter)
                    ){
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_add_a_photo_24),
                            contentDescription = "Placeholder",
                            modifier = Modifier
                                .size(200.dp)
                                .clickable(
                                    onClick = {
                                        launcher.launch("image/*")
                                    })
                                .align(Alignment.TopCenter)
                                .padding(8.dp)
                                .fillMaxWidth()
                                .alpha(if (isVisible) 1f else 0f),
                            tint = Color.Black
                        )
                    }
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopCenter)
                ){
                    imageUri?.let {
                        if (Build.VERSION.SDK_INT < 28) {
                            bitmap.value = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, it)
                            bitmap.value = ImageDecoder.decodeBitmap(source)
                        }

                        bitmap.value?.let {bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Gallery Image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                }
                Column(Modifier.padding(20.dp)) {
                    Spacer(
                        modifier = Modifier
                            .height(210.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        label = {Text(text = "Title")},
                        onValueChange = { title = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor))

                    OutlinedTextField(
                        value = genre,
                        onValueChange = { genre = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                //This value is used to assign to the DropDown the same width
                                textfieldSize = coordinates.size.toSize()
                            },
                        label = {Text("Genre")},
                        trailingIcon = {
                            Icon(icon,"contentDescription",
                                Modifier.clickable { expanded = !expanded })
                        },
                        enabled = false,
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor)

                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(with(LocalDensity.current){textfieldSize.width.toDp()})
                    ) {
                        suggestions.forEach { label ->
                            DropdownMenuItem(onClick = {
                                genre = label
                                expanded = false
                            }) {
                                Text(text = label)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = description,
                        label = {Text(text = "Description")},
                        onValueChange = { description = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.CenterHorizontally),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = rentFee,
                        label = {Text(text = "Rent Fee (per month)")},
                        onValueChange = { rentFee = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
    )
}


@Composable
fun ViewBookScreen(
    navController: NavController,
    id: Int
){
    val state = rememberScrollState()
    LaunchedEffect(Unit) { state.animateScrollTo(150) }
    val userId = Firebase.auth.currentUser!!.uid
    var title: String? = ""
    var imageUrl: String? = ""
    var description: String? = ""
    var genre: String? = ""
    var rentFee: String? = ""
    var bookUserId: String = ""
    var bookId: Int = 0
    val context = LocalContext.current
    val date = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

    val apiInterface = ApiInterface.create().getBook(id).execute()
        if(apiInterface.isSuccessful){
            bookId = apiInterface.body()!!.id!!
            title = apiInterface.body()?.title
            imageUrl = apiInterface.body()?.imageUrl
            description = apiInterface.body()?.description
            genre = apiInterface.body()?.category
            rentFee = apiInterface.body()?.rentPerMonth.toString()
            bookUserId = apiInterface.body()!!.userId
        }


    if(userId == bookUserId) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.Black)
                        }
                    },
                    actions = {
                        // RowScope here, so these icons will be placed horizontally
                        IconButton(onClick = {
                            navController.navigate("editBookDetails/${id}")
                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_edit_24),
                                contentDescription = "Edit",
                                tint = Color.Black
                            )
                        }

                        IconButton(onClick = {
                            showDialog.value = true
                        }) {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_delete_24),
                                contentDescription = "Delete",
                                tint = Color.Black
                            )
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
                                        text = "Are you sure you want to delete this book?",
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
                                            val apiInterface = ApiInterface.create().deleteBook(id)
                                            apiInterface.enqueue(object: Callback<Books> {
                                                override fun onResponse(
                                                    call: Call<Books>,
                                                    response: Response<Books>
                                                ) {
                                                    Log.d("Response", response.code().toString())
                                                    if (progressDialog.isShowing) progressDialog.dismiss()
                                                    Toast.makeText(context, "Book Deleted Successfully!", Toast.LENGTH_LONG).show()
                                                    navController.popBackStack()
                                                    navController.navigate("home") {
                                                        popUpTo("home") {
                                                            inclusive = true
                                                        }
                                                    }
                                                }

                                                override fun onFailure(call: Call<Books>, t: Throwable) {
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
                )
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                ){
                    Column(
                        modifier = Modifier
                            .verticalScroll(state)
                    ){
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )

                        Text(
                            text = "$title" ,
                            style = MaterialTheme.typography.h1,
                            textAlign = TextAlign.Center,
                            fontSize = 30.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Text(
                            text = "Genre: $genre",
                            style = MaterialTheme.typography.body2,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Text(
                            text = "Description:",
                            style = MaterialTheme.typography.body2,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 16.dp, bottom = 4.dp, end = 16.dp)
                        )
                        Text(
                            text = "$description",
                            style = MaterialTheme.typography.body2,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp, bottom = 16.dp, end = 16.dp)
                        )
                        Text(
                            text = "Rent Fee per month: ${rentFee.toString()}",
                            style = MaterialTheme.typography.body2,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        )
    } else {
        var ownerUsername: String = ""
        val apiInterface2 = ApiInterface.create().getUserByUserId(bookUserId).execute()
        if(apiInterface2.isSuccessful){
            ownerUsername = apiInterface2.body()!!.username
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
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
//                    .scrollable(ScrollableState {  })
                ){
                    Column(
                        modifier = Modifier
                            .verticalScroll(state)
                    ){
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = title,
                            modifier = Modifier
                                .size(200.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                        Text(
                            text = "$title" ,
                            style = MaterialTheme.typography.h1,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Text(
                            text = "Owned by: $ownerUsername" ,
                            style = MaterialTheme.typography.h1,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Text(
                            text = "Genre: $genre",
                            style = MaterialTheme.typography.body2,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Text(
                            text = "Description: $description",
                            style = MaterialTheme.typography.body2,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Text(
                            text = "Rent Fee per month: ${rentFee.toString()}",
                            style = MaterialTheme.typography.body2,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        val mYear: Int
                        val mMonth: Int
                        val mDay: Int
                        val now = Calendar.getInstance()
                        var dateStr: String
                        var yearStr: String
                        mYear = now.get(Calendar.YEAR)
                        mMonth = now.get(Calendar.MONTH)
                        mDay = now.get(Calendar.DAY_OF_MONTH)
                        now.time = Date()
                        //val date = remember { mutableStateOf("") }
                        val datePickerDialog = DatePickerDialog(
                            LocalContext.current,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                val cal = Calendar.getInstance()
                                cal.set(year, month, dayOfMonth)
                                //date.value = cal.time.toString()
                                Log.e("Tag", "${cal.time.toString().substring(24..27)}")
                                dateStr = cal.time.toString().substring(0..10)
                                yearStr = cal.time.toString().substring(24..27)
                                date.value = dateStr+yearStr
                            }, mYear, mMonth, mDay
                        )
                        Row() {
                            Button(onClick = {
                                datePickerDialog.show()
                            },
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(text = "Choose a date to rent")
                            }
                            //Spacer(modifier = Modifier.size(16.dp))
                            Text(text = date.value, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if(date.value == "") {
                                    Toast.makeText(context, "Please choose a date first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val progressDialog = ProgressDialog(context)
                                    progressDialog.setMessage("Please wait...")
                                    progressDialog.setCancelable(false)
                                    progressDialog.show() //show progress dialog

                                    val rentRequests = RentRequests(
                                        null,
                                        userId,
                                        bookUserId,
                                        bookId,
                                        date.value,
                                        "Pending"
                                    )
                                    val apiInterface = ApiInterface.create().postRentRequest(rentRequests)
                                    apiInterface.enqueue(object : Callback<RentRequests>{
                                        override fun onResponse(
                                            call: Call<RentRequests>,
                                            response: Response<RentRequests>
                                        ) {
                                            if (progressDialog.isShowing) progressDialog.dismiss()
                                            Log.e("Response", response.code().toString())
                                            Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                            navController.navigate("home") {
                                                popUpTo("home") {
                                                    inclusive = true
                                                }
                                            }
                                        }

                                        override fun onFailure(call: Call<RentRequests>, t: Throwable) {
                                            if (progressDialog.isShowing) progressDialog.dismiss()
                                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                                        }

                                    })
                                }
                            },
                            modifier = Modifier.align(CenterHorizontally),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                top = 12.dp,
                                end = 20.dp,
                                bottom = 12.dp
                            ),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Teal200)
                        ){
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = "Add",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Send Rent Request", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                    }
                }
            }
        )
    }
}

@Composable
fun EditBookScreen(
    navController: NavController,
    id: Int,
    bookCategoriesList: List<BookCategories>
 ){
    val userId = Firebase.auth.currentUser!!.uid
    var title: String = ""
    var imageUrl: String = ""
    var description: String = ""
    var genre: String = ""
    var rentFee: String = ""
    var bookUserId: String = ""

    val apiInterface = ApiInterface.create().getBook(id).execute()
    if(apiInterface.isSuccessful){
        title = apiInterface.body()?.title.toString()
        imageUrl = apiInterface.body()?.imageUrl.toString()
        description = apiInterface.body()?.description.toString()
        genre = apiInterface.body()?.category.toString()
        rentFee = apiInterface.body()?.rentPerMonth.toString()
        bookUserId = apiInterface.body()?.userId.toString()
    }

    val suggestions : MutableList<String> = mutableListOf()
    for (i in bookCategoriesList){
        suggestions.add(i.name)
    }
    val bgColor = Color.White

    suggestions.removeAt(0)
    //Setting up DropdownMenu
    var expanded by remember { mutableStateOf(false) }
    var textfieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    //Setting up ImagePicker from Gallery
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    var editTitle by remember { mutableStateOf(TextFieldValue(title))}
    var editDescription by remember { mutableStateOf(TextFieldValue(description))}
    var editGenre by remember { mutableStateOf(genre) }
    var editRentFee by remember { mutableStateOf(TextFieldValue(rentFee))}
    var downloadUrl: String

    val isVisible: Boolean
    if(imageUri == null){
        isVisible = true
    }
    else{
        isVisible = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = "Edit Book")},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.Black)
                    }
                },
                actions = {
                    // RowScope here, so these icons will be placed horizontally
                    Button(
                        onClick = {

                                if(editTitle.text == "" || editGenre == "" || editDescription.text == "" || editRentFee.text == "" ){
                                    Toast.makeText(context, "Please insert all the details!", Toast.LENGTH_LONG).show()
                                    Log.e("Data", "${editTitle.text}\n ${editGenre}\n" +
                                            " ${editDescription.text}\n" +
                                            " ${editRentFee.text} ")

                                } else { //All are filled with user data

                                    val progressDialog = ProgressDialog(context)
                                    progressDialog.setMessage("Please wait...")
                                    progressDialog.setCancelable(false)
                                    progressDialog.show() //show progress dialog

                                    if(imageUri != null) { //if new image has been detected, upload to firebase
                                        //format image file name
                                        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
                                        val now = Date()
                                        val fileName = formatter.format(now)
                                        //insert into firebase storage
                                        val storageReference = FirebaseStorage.getInstance().getReference()
                                        val ref = storageReference.child("images/$fileName")
                                        val uploadTask = ref.putFile(imageUri!!)
                                        //get url of image uploaded in firebase storage
                                        val urlTask = uploadTask.continueWithTask { task ->
                                            if (!task.isSuccessful) {
                                                task.exception?.let {
                                                    throw it
                                                }
                                            }
                                            ref.downloadUrl
                                        }.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val downloadUri = task.result
                                                Log.e(
                                                    "Firebase Image URL",
                                                    downloadUri.toString()
                                                )
                                                val books = Books(
                                                    id,
                                                    userId,
                                                    editTitle.text.trim(),
                                                    editDescription.text.trim(),
                                                    downloadUri.toString(),
                                                    editGenre,
                                                    editRentFee.text.toDouble())

                                                //call put book api
                                                val apiInterface = ApiInterface.create().putBook(books, id)
                                                apiInterface.enqueue(object: Callback<Books> {
                                                    override fun onResponse(
                                                        call: Call<Books>,
                                                        response: Response<Books>
                                                    ) {
                                                        Log.d("Response", response.code().toString())
                                                        if (progressDialog.isShowing) progressDialog.dismiss()
                                                        Toast.makeText(context, "Book Updated Successfully!", Toast.LENGTH_LONG).show()
                                                        navController.popBackStack()
                                                        navController.navigate("home") {
                                                            popUpTo("home") {
                                                                inclusive = true
                                                            }
                                                        }
                                                    }

                                                    override fun onFailure(call: Call<Books>, t: Throwable) {
                                                        Toast.makeText(context, "Error has Occurred", Toast.LENGTH_LONG).show()
                                                        Log.d("ERROR", t.toString())
                                                    }

                                                })
                                            } else {
                                                // Handle failures
                                                Toast.makeText(context, "Firebase Error in Obtaining Image URL", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else { //if there is no new image, just get the old image url
                                        val books = Books(
                                            id,
                                            userId,
                                            editTitle.text.trim(),
                                            editDescription.text.trim(),
                                            imageUrl,
                                            editGenre,
                                            editRentFee.text.toDouble())
                                        //call put book api
                                        val apiInterface = ApiInterface.create().putBook(books, id)
                                        apiInterface.enqueue(object: Callback<Books> {
                                            override fun onResponse(
                                                call: Call<Books>,
                                                response: Response<Books>
                                            ) {
                                                Log.d("Response", response.code().toString())
                                                if (progressDialog.isShowing) progressDialog.dismiss()
                                                Toast.makeText(context, "Book Updated Successfully!", Toast.LENGTH_LONG).show()
                                                navController.popBackStack()
                                                navController.navigate("home") {
                                                    popUpTo("home") {
                                                        inclusive = true
                                                    }
                                                }
                                            }

                                            override fun onFailure(call: Call<Books>, t: Throwable) {
                                                Toast.makeText(context, "Error has Occurred", Toast.LENGTH_LONG).show()
                                                Log.d("ERROR", t.toString())
                                            }

                                        })
                                    }
                                }
                        },
                        // Uses ButtonDefaults.ContentPadding by default
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 12.dp,
                            end = 20.dp,
                            bottom = 12.dp
                        ),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Teal200)
                    ) {
                        // Inner content including an icon and a text label
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_edit_24),
                            contentDescription = "Edit",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "Edit")
                    }

                }
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
//                    .scrollable(ScrollableState {  })
            ){
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopCenter)
                ){
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier
                            .size(200.dp)
                            .clickable(
                                onClick = {
                                    launcher.launch("image/*")
                                })
                            .align(Alignment.TopCenter)
                            .padding(8.dp)
                            .fillMaxWidth()
                            .alpha(if (isVisible) 1f else 0f),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopCenter)
                ){
                    imageUri?.let {
                        if (Build.VERSION.SDK_INT < 28) {
                            bitmap.value = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, it)
                            bitmap.value = ImageDecoder.decodeBitmap(source)
                        }

                        bitmap.value?.let {bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Gallery Image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                }
                Column(Modifier.padding(20.dp)) {
                    Spacer(
                        modifier = Modifier
                            .height(210.dp)
                    )
                    OutlinedTextField(
                        value = editTitle,
                        label = {Text(text = "Title")},
                        onValueChange = { editTitle = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor))

                    OutlinedTextField(
                        value = editGenre,
                        onValueChange = { editGenre = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                //This value is used to assign to the DropDown the same width
                                textfieldSize = coordinates.size.toSize()
                            },
                        label = {Text("Genre")},
                        trailingIcon = {
                            Icon(icon,"contentDescription",
                                Modifier.clickable { expanded = !expanded })
                        },
                        enabled = false,
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor)

                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(with(LocalDensity.current){textfieldSize.width.toDp()})
                    ) {
                        suggestions.forEach { label ->
                            DropdownMenuItem(onClick = {
                                editGenre = label
                                expanded = false
                            }) {
                                Text(text = label)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = editDescription,
                        label = {Text(text = "Description")},
                        onValueChange = { editDescription = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.CenterHorizontally),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = editRentFee,
                        label = {Text(text = "Rent Fee (per month)")},
                        onValueChange = { editRentFee = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = bgColor),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
    )
}




