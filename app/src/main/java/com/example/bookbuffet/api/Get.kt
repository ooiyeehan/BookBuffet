package com.example.bookbuffet.api

import com.example.bookbuffet.model.BookCategories
import com.example.bookbuffet.model.Books
import retrofit2.Response
import retrofit2.http.GET

interface Get {
    @GET("api/Books")
    suspend fun getBooksMVVM() : Response<List<Books>>

    @GET("api/BookCategories")
    suspend fun getBookCategoriesMVVM() : Response<List<BookCategories>>
}