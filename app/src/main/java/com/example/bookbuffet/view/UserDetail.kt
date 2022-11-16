package com.example.bookbuffet.view

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookbuffet.R
import com.example.bookbuffet.api.ApiInterface
import com.example.bookbuffet.model.BookCategories
import com.example.bookbuffet.model.Books
import com.example.bookbuffet.model.Users
import com.example.bookbuffet.ui.theme.Teal200
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserDetails(navController: NavController, email: String) {
    var id: Int = 0
    var username: String = ""
    var profileImageUrl: String = ""
    var password: String = ""


    //Setting up ImagePicker from Gallery
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    val isVisible: Boolean
    if(imageUri == null){
        isVisible = true
    }
    else{
        isVisible = false
    }

    val apiInterface = ApiInterface.create().getUser(email).execute()
    if(apiInterface.isSuccessful){
        id = apiInterface.body()?.id!!
        username = apiInterface.body()?.username.toString()
        profileImageUrl = apiInterface.body()?.profileImageUrl.toString()
        password = apiInterface.body()?.password.toString()
    }
    var editUsername by remember { mutableStateOf(TextFieldValue(username))}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = "Your Profile")},
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
                    .background(Color.White)
            ){
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopCenter)
                ){
                    if(profileImageUrl == "")
                    {
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
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = username,
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
                        value = editUsername,
                        label = {Text(text = "Username")},
                        onValueChange = { editUsername = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if(editUsername.text == ""){
                                Toast.makeText(context, "Username must not be empty!", Toast.LENGTH_LONG).show()
                            } else {
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
                                    val ref = storageReference.child("userImages/$fileName")
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
                                            val users = Users(
                                                id,
                                                Firebase.auth.currentUser!!.uid,
                                                editUsername.text.trim(),
                                                email,
                                                password,
                                                downloadUri.toString()
                                            )

                                            //call put user api
                                            val apiInterface = ApiInterface.create().putUser(users, id)
                                            apiInterface.enqueue(object: Callback<Users> {
                                                override fun onResponse(
                                                    call: Call<Users>,
                                                    response: Response<Users>
                                                ) {
                                                    Log.d("Response", response.code().toString())
                                                    if (progressDialog.isShowing) progressDialog.dismiss()
                                                    Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_LONG).show()
                                                    navController.popBackStack()
                                                    navController.navigate("home") {
                                                        popUpTo("home") {
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
                                            // Handle failures
                                            Toast.makeText(context, "Firebase Error in Obtaining Image URL", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else { //if there is no new image, just get the old image url
                                    val users = Users(
                                        id,
                                        Firebase.auth.currentUser!!.uid,
                                        editUsername.text.trim(),
                                        email,
                                        password,
                                        profileImageUrl
                                    )
                                    //call put book api
                                    val apiInterface = ApiInterface.create().putUser(users, id)
                                    apiInterface.enqueue(object: Callback<Users> {
                                        override fun onResponse(
                                            call: Call<Users>,
                                            response: Response<Users>
                                        ) {
                                            Log.d("Response", response.code().toString())
                                            if (progressDialog.isShowing) progressDialog.dismiss()
                                            Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_LONG).show()
                                            navController.popBackStack()
                                            navController.navigate("home") {
                                                popUpTo("home") {
                                                    inclusive = true
                                                }
                                            }
                                        }

                                        override fun onFailure(call: Call<Users>, t: Throwable) {
                                            Toast.makeText(context, "Error has Occurred", Toast.LENGTH_LONG).show()
                                            Log.d("ERROR", t.toString())
                                        }

                                    })
                                }
                            }

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
                            painterResource(id = R.drawable.ic_baseline_edit_24),
                            contentDescription = "Edit",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "Update Profile")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

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
            }
        }
    )

}
