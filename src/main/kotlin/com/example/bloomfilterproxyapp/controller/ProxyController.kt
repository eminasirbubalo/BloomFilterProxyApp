package com.example.bloomfilterproxyapp.controller
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.example.bloomfilterproxyapp.service.UrlCheckService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URI

@RestController
@RequestMapping("/api")
class ProxyController(private val urlCheckService: UrlCheckService) {

    @GetMapping("/proxy")
    fun handleRequest(@RequestParam url: String): ResponseEntity<Any> {
        var finalUrl = url.trim()

        // Dodaj protokol ako nedostaje da URI ne pukne
        if (!finalUrl.startsWith("http")) {
            finalUrl = "https://$finalUrl"
        }

        val isSafe = urlCheckService.checkIfUrlIsSafe(finalUrl)

        return if (isSafe) {
            // Šaljemo browser na vanjsku stranicu
            ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(finalUrl))
                .build()
        } else {
            // Ostajemo na našoj stranici s porukom
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_HTML)
                .body("""
                    <div style="text-align:center; margin-top:50px; font-family:sans-serif;">
                        <h1 style="color:red;">PRISTUP BLOKIRAN!</h1>
                        <p>Stranica <b>$finalUrl</b> je prepoznata kao phishing prijetnja.</p>
                        <a href="/">Vrati se na pocetnu</a>
                    </div>
                """.trimIndent())
        }
    }
}