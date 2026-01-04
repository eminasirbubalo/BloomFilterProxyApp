package com.example.bloomfilterproxyapp.filter

interface MembershipFilter {
    fun add(url: String)
    fun mightContain(url: String): Boolean
}
