package com.example.bloomfilterproxyapp.service

import com.example.bloomfilterproxyapp.filter.*
import com.example.bloomfilterproxyapp.model.Stats
import com.example.bloomfilterproxyapp.model.Url
import org.springframework.stereotype.Service
import com.example.bloomfilterproxyapp.repository.UrlRepository

@Service
class UrlCheckService(
    private val repository: UrlRepository,
    private val dataLoaderService: DataLoaderService,
    private val bloomFilter: BloomFilter,
    private val singleHashBloom: SingleHashBloomFilter,
    private val countingBloom: CountingBloomFilter,
    private val cuckooFilter: CuckooFilter
) {

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
        bloomFilter.add(normalizedUrl)
        repository.save(Url(normalizedUrl, type))
    }

//    fun checkIfUrlIsSafe(url: String): Boolean {
//        val normalizedUrl = normalizeUrl(url)
//        if (!bloomFilter.mightContain(normalizedUrl)) {
//            return true
//        }
//        return !repository.existsById(normalizedUrl)
//    }

    fun checkIfUrlIsSafe(url: String): Boolean {
        val normalizedUrl = normalizeUrl(url)
        println("🔍 PROXY CHECK: original='$url' → normalized='$normalizedUrl'")

        val inBloom = bloomFilter.mightContain(normalizedUrl)
        println("   Bloom filter kaže: $inBloom")

        if (!inBloom) {
            println("   ✅ SIGURAN (nije u Bloom filteru)")
            return true
        }

        val inDb = repository.existsById(normalizedUrl)
        println("   Baza kaže: $inDb")

        val isSafe = !inDb
        println("   FINALNA ODLUKA: ${if (isSafe) "✅ SIGURAN" else "❌ BLOKIRAN"}")
        return isSafe
    }


    fun runCsvBenchmark(): Map<String, Any> {
        val (malicious, benign) = dataLoaderService.loadFromCsvForBenchmark(
            maxMalicious = 100_000,
            maxBenign = 100_000
        )

        val filters: Map<String, MembershipFilter> = mapOf(
            "bloom_optimal" to bloomFilter,
            "bloom_single_hash" to singleHashBloom,
            "counting_bloom" to countingBloom,
            "cuckoo" to cuckooFilter
        )

        val statsMap = mutableMapOf<String, Stats>()

        // Insert faza u filtere
        for ((name, filter) in filters) {
            val stats = Stats()
            val start = System.nanoTime()
            malicious.forEach { filter.add(it) }
            val end = System.nanoTime()
            stats.insertTimeMs = (end - start) / 1_000_000
            statsMap[name] = stats
        }

        // Lookup faza na malicioznim i benign URL-ovima
        for ((name, filter) in filters) {
            val stats = statsMap[name]!!

            var start = System.nanoTime()
            malicious.forEach {
                val res = filter.mightContain(it)
                if (res) stats.tp++ else stats.fn++
            }
            var end = System.nanoTime()
            stats.lookupTimeMs += (end - start) / 1_000_000

            start = System.nanoTime()
            benign.forEach {
                val res = filter.mightContain(it)
                if (res) stats.fp++ else stats.tn++
            }
            end = System.nanoTime()
            stats.lookupTimeMs += (end - start) / 1_000_000
        }

        // Referenca – čista baza
        val dbStats = Stats()
        val startDb = System.nanoTime()
        malicious.forEach {
            val found = repository.existsById(it)
            if (found) dbStats.tp++ else dbStats.fn++
        }
        benign.forEach {
            val found = repository.existsById(it)
            if (found) dbStats.fp++ else dbStats.tn++
        }
        val endDb = System.nanoTime()
        dbStats.lookupTimeMs = (endDb - startDb) / 1_000_000

        val totalTested = malicious.size + benign.size
        val dbOnlyTime = dbStats.lookupTimeMs.toDouble()
        val bloomStats = statsMap["bloom_optimal"]!!
        val bloomTime = bloomStats.lookupTimeMs.toDouble()
        val improvement = if (dbOnlyTime == 0.0) 0.0 else (dbOnlyTime - bloomTime) / dbOnlyTime * 100.0

        return mapOf(
            "total_urls_tested" to totalTested,
            "db_only_time_ms" to dbOnlyTime,
            "bloom_hybrid_time_ms" to bloomTime,
            "performance_improvement_percent" to String.format("%.2f %%", improvement),
            "filters" to statsMap.mapValues { (_, s) ->
                mapOf(
                    "insert_time_ms" to s.insertTimeMs,
                    "lookup_time_ms" to s.lookupTimeMs,
                    "tp" to s.tp,
                    "fp" to s.fp,
                    "tn" to s.tn,
                    "fn" to s.fn,
                    "false_positive_rate" to s.falsePositiveRate,
                    "false_negative_rate" to s.falseNegativeRate
                )
            },
            "database" to mapOf(
                "lookup_time_ms" to dbStats.lookupTimeMs,
                "tp" to dbStats.tp,
                "fp" to dbStats.fp,
                "tn" to dbStats.tn,
                "fn" to dbStats.fn,
                "false_positive_rate" to dbStats.falsePositiveRate,
                "false_negative_rate" to dbStats.falseNegativeRate
            )
        )
    }
}
