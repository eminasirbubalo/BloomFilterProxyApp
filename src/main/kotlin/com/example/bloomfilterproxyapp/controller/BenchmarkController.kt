package com.example.bloomfilterproxyapp.controller

import com.example.bloomfilterproxyapp.service.UrlCheckService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class BenchmarkController(private val urlCheckService: UrlCheckService) {

    @GetMapping("/benchmark")
    fun startBenchmark(): Map<String, Any> {
        val testUrls = listOf(
            "google.com", "https://www.youtube.com", "facebook.com",
            "amazon.co.uk", "netflix.com", "https://reddit.com",
            "facebook.unitedcolleges.net", "paypal-security-update.com",
            "microsoft.com", "github.com", "stackoverflow.com",
            "malicious-site.net/login", "http://bank-verify.info"
        )

        return urlCheckService.runBenchmark(testUrls)
    }
}