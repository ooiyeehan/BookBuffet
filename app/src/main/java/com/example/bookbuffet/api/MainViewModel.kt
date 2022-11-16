package com.example.bookbuffet.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookbuffet.model.BookCategories
import com.example.bookbuffet.model.Books
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val repo: Repo) : ViewModel() {

    private val _books: MutableLiveData<Response<List<Books>>> = MutableLiveData()
    val books: LiveData<Response<List<Books>>> = _books
    val bookCategories: MutableLiveData<Response<List<BookCategories>>> = MutableLiveData()

    fun getBooksMVVM() {
        viewModelScope.launch {
            try{
                val response = repo.getBooksMVVM()
                _books.value = response
            }catch(e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun  getBookCategoriesMVVM() {
        viewModelScope.launch {
            try{
                val response = repo.getBookCategoriesMVVM()
                bookCategories.value = response
            }catch(e: Exception){
                e.printStackTrace()
            }
        }
    }
}