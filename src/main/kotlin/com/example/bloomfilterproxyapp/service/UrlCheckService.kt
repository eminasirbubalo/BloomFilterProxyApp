package com.example.bloomfilterproxyapp.service

import com.example.bloomfilterproxyapp.filter.BloomFilter
import com.example.bloomfilterproxyapp.model.Url
import org.springframework.stereotype.Service
import com.example.bloomfilterproxyapp.repository.UrlRepository

@Service
class UrlCheckService(private val repository: UrlRepository) {

    // m i k će se automatski izračunati u ManualBloomFilter klasi
    private val bloomFilter = BloomFilter(650000, 0.01)

    private fun normalizeUrl(url: String): String {
        return url.lowercase()
            .replace("http://", "")
            .replace("https://", "")
            .replace("www.", "")
            .removeSuffix("/")
            .trim()
    }

    fun addToBlacklist(url: String, type: String) {
        val normalizedUrl = normalizeUrl(url)
        // 1. Dodaj u Bloom Filter (u RAM-u)
        bloomFilter.add(url)

        // 2. Dodaj u H2 Bazu (na disk/trajno)
        // Napomena: Ovo može biti sporo za 650k zapisa.
        // Ako ti baza ne treba za kolegij, možeš ovaj dio zakomentirati
        repository.save(Url(url, type))
    }

    fun checkIfUrlIsSafe(url: String): Boolean {
        val normalizedUrl = normalizeUrl(url)
        // Prva linija obrane: Bloom Filter
        if (!bloomFilter.mightContain(normalizedUrl)) {
            return true
        }
        return false
        // Druga linija: Baza (provjera je li stvarno tamo ili je False Positive)
        // return !repository.existsById(url)
    }
}