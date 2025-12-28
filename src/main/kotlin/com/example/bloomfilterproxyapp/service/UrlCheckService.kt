package com.example.bloomfilterproxyapp.service

import com.example.bloomfilterproxyapp.filter.BloomFilter
import com.example.bloomfilterproxyapp.model.Url
import org.springframework.stereotype.Service
import com.example.bloomfilterproxyapp.repository.UrlRepository
import kotlin.system.measureNanoTime

@Service
class UrlCheckService(private val repository: UrlRepository) {

    // m i k će se automatski izračunati u ManualBloomFilter klasi
    private val bloomFilter = BloomFilter(650000, 0.01)

    private fun normalizeUrl(url: String): String {
        return url.lowercase()
            .replaceFirst("https://", "")
            .replaceFirst("http://", "")
            .replaceFirst("www.", "")
            .removeSuffix("/")
            .trim()
    }

    fun addToBlacklist(url: String, type: String) {
        val normalizedUrl = normalizeUrl(url)
        // 1. Dodaj u Bloom Filter (u RAM-u)
        bloomFilter.add(normalizedUrl)

        // 2. Dodaj u H2 Bazu (na disk/trajno)
        // Napomena: Ovo može biti sporo za 650k zapisa.
        // Ako ti baza ne treba za kolegij, možeš ovaj dio zakomentirati
        repository.save(Url(normalizedUrl, type))
    }

    fun checkIfUrlIsSafe(url: String): Boolean {
        val normalizedUrl = normalizeUrl(url)

        if (!bloomFilter.mightContain(normalizedUrl)) {
            return true
        }
        return !repository.existsById(normalizedUrl)
    }

    fun runBenchmark(testUrls: List<String>): Map<String, Any> {
        val total = testUrls.size

        // 1. Mjerenje: Samo Baza
        val timeDbOnly = measureNanoTime {
            testUrls.forEach {
                repository.existsById(normalizeUrl(it))
            }
        } / 1_000_000.0 // Prebacujemo u milisekunde

        // 2. Mjerenje: Bloom Filter + Baza (Hibrid)
        val timeWithBloom = measureNanoTime {
            testUrls.forEach {
                val clean = normalizeUrl(it)
                if (bloomFilter.mightContain(clean)) {
                    repository.existsById(clean)
                }
            }
        } / 1_000_000.0

        val improvement = ((timeDbOnly - timeWithBloom) / timeDbOnly) * 100

        return mapOf(
            "total_urls_tested" to total,
            "db_only_time_ms" to "%.3f".format(timeDbOnly),
            "bloom_hybrid_time_ms" to "%.3f".format(timeWithBloom),
            "performance_improvement_percent" to "${"%.2f".format(improvement)}%"
        )
    }
}