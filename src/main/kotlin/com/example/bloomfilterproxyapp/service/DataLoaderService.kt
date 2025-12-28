package com.example.bloomfilterproxyapp.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import com.example.bloomfilterproxyapp.repository.UrlRepository
import java.io.File

@Service
class DataLoaderService(
    private val urlScannerService: UrlCheckService,
    private val repository: UrlRepository
) {

    @PostConstruct
    fun loadMaliciousUrls() {
        // Putanja do tvog CSV fajla
        val csvFile = File("src/main/resources/malicious_phish.csv")

        if (!csvFile.exists()) {
            println("GRESKA: Datoteka nije pronađena na lokaciji: ${csvFile.absolutePath}")
            return
        }

        println("Započeto učitavanje podataka...")

        var count = 0
        val maxRecords = 100000 // Ograničenje za testiranje

        csvFile.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line -> // Preskačemo header (url,type)
                if (count >= maxRecords) return@forEach

                // 1. Pronalazimo zadnji zarez (jer labela 'type' nema zareza, a URL može imati)
                val lastCommaIndex = line.lastIndexOf(',')

                if (lastCommaIndex != -1) {
                    // 2. Izdvajamo URL i TIP
                    val rawUrl = line.substring(0, lastCommaIndex).replace("\"", "").trim()
                    val typeValue = line.substring(lastCommaIndex + 1).replace("\"", "").trim()

                    // 3. NORMALIZACIJA - Čistimo URL od protokola i www
                    val cleanUrl = normalizeUrl(rawUrl)

                    // 4. Provjera tipa (dodajemo samo ako nije benign)
                    if (typeValue.lowercase() != "benign") {
                        try {
                            // Ova metoda u urlScannerService treba dodati URL i u bazu i u Bloom Filter
                            urlScannerService.addToBlacklist(cleanUrl, typeValue)
                            count++
                        } catch (e: Exception) {
                            println("Greška pri dodavanju reda: $line | Greška: ${e.message}")
                        }
                    }
                }

                // Ispis napretka svakih 10.000 zapisa
                if (count > 0 && count % 10000 == 0) {
                    println("Učitano $count malicioznih URL-ova...")
                }
            }
        }

        println("Učitavanje završeno! Ukupno dodano malicioznih URL-ova: $count")
    }

    private fun normalizeUrl(url: String): String {
        return url.trim()
            .lowercase()
            .replaceFirst(Regex("^https?://"), "") // Uklanja http:// ili https://
            .replaceFirst(Regex("^www\\."), "")    // Uklanja www.
            .removeSuffix("/")                    // Uklanja kosi zarez na kraju
    }
}