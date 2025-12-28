package com.example.bloomfilterproxyapp.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Url(
    @Id
    @Column(length = 2048) // Ovo rješava "Value too long" problem
    val url: String = "",

    @Column(length = 100) // Tip (phishing, malware) je obično kratak, ali možeš i njega povećati
    val type: String = ""
)