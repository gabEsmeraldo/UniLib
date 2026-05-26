package com.example.unilib.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Book(
    var id: String = "",
    var title: String = "",
    var author: String = "",
    var isbn: String = "",
    var tags: List<String> = emptyList(),
    var synopsis: String = "",
    var quantity: Long = 0,
    var available: Long = 0,
    var lentCount: Long = 0,
    var imageUrl: String = "",
    @ServerTimestamp
    var createdAt: Date? = null
) {
    constructor() : this("", "", "", "", emptyList(), "", 0, 0, 0, "", null)
}