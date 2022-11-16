package com.example.bookbuffet.model

data class RentRequests(
    val id: Int?,
    val requesterId: String,
    val receiverId: String,
    val bookId: Int,
    val requestDate: String,
    val status: String
)

