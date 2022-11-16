package com.example.bookbuffet

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookbuffet.api.MainViewModel
import com.example.bookbuffet.api.MainViewModelFactory
import com.example.bookbuffet.api.Repo
import com.example.bookbuffet.model.BookCategories
import com.example.bookbuffet.model.Books
import com.example.bookbuffet.ui.theme.BookBuffetTheme
import com.example.bookbuffet.view.*


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt > 8) {
            val policy = ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        val context: ComponentActivity = this
        var booksList  = emptyList<Books>()
        var bookCategoriesList = emptyList<BookCategories>()

        val repository = Repo()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        viewModel.getBooksMVVM()
        viewModel.books.observe(this, Observer { response ->
            //pass list here
            if (response.isSuccessful){
                booksList = response.body()!!
            }  else {
                //error
            }
        })

        viewModel.getBookCategoriesMVVM()
        viewModel.bookCategories.observe(this, Observer { response ->
            if (response.isSuccessful){
                bookCategoriesList = response.body()!!
            } else {
                //error
            }
        })
        setContent {
            BookBuffetTheme {
                //AnimatedSplashScreen()
                    val navController =  rememberNavController()
                    NavHost(navController, startDestination = "splash"){

                        composable("splash") { AnimatedSplashScreen(navController, context)}
                        composable("signin") { SignIn(navController, context)}
                        composable("signup") { SignUp(navController, context)}
                        composable("home") {
                            onResume()
                            HomeScreen(navController,booksList,bookCategoriesList, viewModel)
                        }
                        composable("library") { LibraryScreen(navController) }
                        composable("create") { AddBookScreen(navController, bookCategoriesList) }
                        composable("searchScreen") { Search(navController) }
                        composable(
                            "searchResult/{title}",
                            arguments = listOf(navArgument("title") {type = NavType.StringType})
                        ) { backStackEntry -> backStackEntry.arguments?.getString("title")
                            ?.let { SearchResult(navController, it) } }
                        composable(
                            "userBooks/{userId}",
                            arguments = listOf(navArgument("userId") {type = NavType.StringType})
                        ) { backStackEntry -> backStackEntry.arguments?.getString("userId")
                            ?.let { UserBooks(navController, it) } }
                        composable(
                            "bookDetails/{id}",
                            arguments = listOf(navArgument("id") {type = NavType.IntType})
                        ) { backStackEntry -> backStackEntry.arguments?.getInt("id")
                            ?.let { ViewBookScreen(navController, it) } }
                        composable(
                            "editBookDetails/{id}",
                            arguments = listOf(navArgument("id") {type = NavType.IntType})
                        ) { backStackEntry -> backStackEntry.arguments?.getInt("id")
                            ?.let { EditBookScreen(navController, it, bookCategoriesList) } }
                        composable(
                            "userDetails/{email}",
                            arguments = listOf(navArgument("email") {type = NavType.StringType})
                        ) { backStackEntry -> backStackEntry.arguments?.getString("email")
                            ?.let { UserDetails(navController, it) } }
                        composable(
                            "pendingRequests/{pending}",
                            arguments = listOf(navArgument("pending") {type = NavType.StringType})
                        ) { backStackEntry -> backStackEntry.arguments?.getString("pending")
                            ?.let { PendingRequests(navController, it) } }
                        composable(
                            "rentHistory/{done}",
                            arguments = listOf(navArgument("done") {type = NavType.StringType})
                        ) { backStackEntry -> backStackEntry.arguments?.getString("done")
                            ?.let { RentHistory(navController, it) } }
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        var booksList  = emptyList<Books>()
        val repository = Repo()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        viewModel.getBooksMVVM()
        viewModel.books.observe(this, Observer { response ->
            //pass list here
            if (response.isSuccessful){
                booksList = response.body()!!
            }  else {
                //error
            }
        })
    }

}



