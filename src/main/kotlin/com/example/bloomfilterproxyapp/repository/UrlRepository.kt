package com.example.bloomfilterproxyapp.repository

import com.example.bloomfilterproxyapp.model.Url
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UrlRepository : JpaRepository<Url, String> {
    }