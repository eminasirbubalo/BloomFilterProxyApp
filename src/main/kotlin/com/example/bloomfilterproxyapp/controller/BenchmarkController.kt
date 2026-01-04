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
        return urlCheckService.runCsvBenchmark()
    }
}