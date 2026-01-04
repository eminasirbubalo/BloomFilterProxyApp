package com.example.bloomfilterproxyapp.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import com.example.bloomfilterproxyapp.repository.UrlRepository
import com.example.bloomfilterproxyapp.filter.BloomFilter
import com.example.bloomfilterproxyapp.model.Url
import java.io.File

@Service
class DataLoaderService(
    private val repository: UrlRepository,
    private val bloomFilter: BloomFilter
) {

    @PostConstruct
    fun loadMaliciousUrls() {
        val csvFile = File("src/main/resources/malicious_phish.csv")

        if (!csvFile.exists()) {
            println("GRESKA: Datoteka nije pronađena na lokaciji: ${csvFile.absolutePath}")
            return
        }

        println("Započeto učitavanje podataka...")

        var count = 0
        // UKLONJEN LIMIT - učitava SVE maliciozne URL-ove iz CSV-a

        csvFile.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                // UKLONJEN CHECK: if (count >= maxRecords) return@forEach

                val lastCommaIndex = line.lastIndexOf(',')

                if (lastCommaIndex != -1) {
                    val rawUrl = line.substring(0, lastCommaIndex).replace("\"", "").trim()
                    val typeValue = line.substring(lastCommaIndex + 1).replace("\"", "").trim()

                    val cleanUrl = normalizeUrl(rawUrl)

                    if (typeValue.lowercase() != "benign") {
                        try {
                            repository.save(Url(cleanUrl, typeValue))
                            bloomFilter.add(cleanUrl)
                            count++
                        } catch (e: Exception) {
                            println("Greška pri dodavanju reda: $line | Greška: ${e.message}")
                        }
                    }
                }

                if (count > 0 && count % 50000 == 0) {
                    println("Učitano $count malicioznih URL-ova...")
                }
            }
        }

        println("Učitavanje završeno! Ukupno dodano malicioznih URL-ova: $count")
        println("✅ BloomFilter je napunjen sa $count URL-ova")
    }

    fun loadFromCsvForBenchmark(
        maxMalicious: Int = 100_000,
        maxBenign: Int = 100_000
    ): Pair<List<String>, List<String>> {
        val csvFile = File("src/main/resources/malicious_phish.csv")
        if (!csvFile.exists()) {
            println("GRESKA: Datoteka nije pronađena na lokaciji ${csvFile.absolutePath}")
            return emptyList<String>() to emptyList()
        }

        val malicious = mutableListOf<String>()
        val benign = mutableListOf<String>()

        csvFile.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val lastCommaIndex = line.lastIndexOf(',')
                if (lastCommaIndex == -1) return@forEach

                val rawUrl = line.substring(0, lastCommaIndex).replace("\"", "").trim()
                val typeValue = line.substring(lastCommaIndex + 1).replace("\"", "").trim()

                val cleanUrl = normalizeUrl(rawUrl)

                if (typeValue.lowercase() == "benign") {
                    if (benign.size < maxBenign) benign += cleanUrl
                } else {
                    if (malicious.size < maxMalicious) malicious += cleanUrl
                }

                if (malicious.size >= maxMalicious && benign.size >= maxBenign) return@useLines
            }
        }

        println("Benchmark: učitano ${malicious.size} malicioznih i ${benign.size} benign URL-ova")
        return malicious to benign
    }

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .lowercase()
            .replaceFirst(Regex("^https?://"), "")
            .replaceFirst(Regex("^www\\."), "")
            .removeSuffix("/")
    }
}
