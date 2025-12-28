package com.example.bloomfilterproxyapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BloomFilterProxyAppApplication

fun main(args: Array<String>) {
    runApplication<BloomFilterProxyAppApplication>(*args)
}
