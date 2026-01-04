package com.example.bloomfilterproxyapp.filter

import kotlin.math.*
import kotlin.math.roundToInt

class CountingBloomFilter(
    val n: Int,
    val p: Double
): MembershipFilter {
    private val m: Int = (n * ln(p) / -(ln(2.0).pow(2.0))).toInt()
    private val k: Int = max(1, (m.toDouble() / n * ln(2.0)).roundToInt())
    private val counters = IntArray(m)

    override fun add(url: String) {
        val (h1, h2) = hashes(url)
        for (i in 0 until k) {
            val idx = index(h1, h2, i)
            if (counters[idx] < Int.MAX_VALUE) counters[idx]++
        }
    }

    fun remove(url: String) {
        val (h1, h2) = hashes(url)
        for (i in 0 until k) {
            val idx = index(h1, h2, i)
            if (counters[idx] > 0) counters[idx]--
        }
    }

    override fun mightContain(url: String): Boolean {
        val (h1, h2) = hashes(url)
        for (i in 0 until k) {
            val idx = index(h1, h2, i)
            if (counters[idx] == 0) return false
        }
        return true
    }

    private fun hashes(url: String): Pair<Int, Int> {
        val h1 = url.hashCode()
        val h2 = hashFNV1a(url)
        return h1 to h2
    }

    private fun index(h1: Int, h2: Int, i: Int): Int {
        val v = h1 + i * h2
        return kotlin.math.abs(v % m)
    }

    private fun hashFNV1a(url: String): Int {
        var hash = -0x7ee3623b
        for (char in url) {
            hash = hash xor char.code
            hash *= 0x01000193
        }
        return hash
    }
}
