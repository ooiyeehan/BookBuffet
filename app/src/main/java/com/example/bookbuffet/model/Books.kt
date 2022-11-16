package com.example.bookbuffet.model

data class Books(
    val id: Int?,
    val userId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val rentPerMonth: Double
)
