package com.example.bloomfilterproxyapp.controller
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.example.bloomfilterproxyapp.service.UrlCheckService
import java.net.URI

@RestController
class ProxyController(private val urlCheckService: UrlCheckService) {

    @GetMapping("/proxy")
    fun handleRequest(@RequestParam url: String): ResponseEntity<Any> {
        // 1. Očistimo razmake
        var finalUrl = url.trim()

        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }

        val isSafe = urlCheckService.checkIfUrlIsSafe(finalUrl)

        return if (isSafe) {
            // Sada URI.create dobiva npr. "https://www.google.com" i zna napraviti vanjski redirect
            ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(finalUrl))
                .build()
        } else {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("<h1>Blokirano!</h1><p>URL $finalUrl je opasan.</p>")
        }
    }
}