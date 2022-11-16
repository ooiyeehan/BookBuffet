package com.example.bookbuffet.api

import com.example.bookbuffet.model.BookCategories
import com.example.bookbuffet.model.Books
import retrofit2.Response

class Repo{
    suspend fun getBooksMVVM(): Response<List<Books>> {
        return RetrofitInstance.api.getBooksMVVM()
    }

    suspend fun getBookCategoriesMVVM() : Response<List<BookCategories>> {
        return RetrofitInstance.api.getBookCategoriesMVVM()
    }
}