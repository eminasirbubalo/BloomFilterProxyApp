package com.example.bloomfilterproxyapp.config

import com.example.bloomfilterproxyapp.filter.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig {

    @Bean
    fun bloomFilter(): BloomFilter {
        return BloomFilter(650000, 0.01)
    }

    @Bean
    fun singleHashBloomFilter(bloomFilter: BloomFilter): SingleHashBloomFilter {
        return SingleHashBloomFilter(m = bloomFilter.m)
    }

    @Bean
    fun countingBloomFilter(): CountingBloomFilter {
        return CountingBloomFilter(650000, 0.01)
    }

    @Bean
    fun cuckooFilter(): CuckooFilter {
        return CuckooFilter(bucketCount = 200_000, bucketSize = 4)
    }
}
