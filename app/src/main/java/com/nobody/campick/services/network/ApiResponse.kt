package com.nobody.campick.services.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

@Serializable
data class Page<T>(
    val content: List<T>,
    val pageable: Pageable,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val size: Int,
    val number: Int,
    val sort: Sort,
    val numberOfElements: Int,
    val first: Boolean,
    val empty: Boolean
) {
    companion object {
        fun <T> empty(): Page<T> = Page(
            content = emptyList(),
            pageable = Pageable.unpaged(),
            totalElements = 0,
            totalPages = 0,
            last = true,
            size = 0,
            number = 0,
            sort = Sort.unsorted(),
            numberOfElements = 0,
            first = true,
            empty = true
        )
    }
}

@Serializable
data class Pageable(
    val sort: Sort,
    val offset: Long,
    val pageSize: Int,
    val pageNumber: Int,
    val paged: Boolean,
    val unpaged: Boolean
) {
    companion object {
        fun unpaged(): Pageable = Pageable(
            sort = Sort.unsorted(),
            offset = 0,
            pageSize = 0,
            pageNumber = 0,
            paged = false,
            unpaged = true
        )
    }
}

@Serializable
data class Sort(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean
) {
    companion object {
        fun unsorted(): Sort = Sort(
            empty = true,
            sorted = false,
            unsorted = true
        )
    }
}