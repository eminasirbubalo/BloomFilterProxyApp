package com.example.bloomfilterproxyapp.filter
import java.util.BitSet
import kotlin.math.*
import kotlin.text.iterator

class BloomFilter(val n: Int, val p: Double) {
    private val m: Int = (- (n * ln(p)) / (ln(2.0).pow(2.0))).toInt()
    private val k: Int = max(1, (m.toDouble() / n * ln(2.0)).roundToInt())
    private val bitSet = BitSet(m)

    fun add(url: String) {
        val h1 = url.hashCode()
        val h2 = hashFNV1a(url)
        for (i in 0 until k) {
            val index = abs((h1 + i * h2) % m)
            bitSet.set(index)
        }
    }

    fun mightContain(url: String): Boolean {
        val h1 = url.hashCode()
        val h2 = hashFNV1a(url)
        for (i in 0 until k) {
            val index = abs((h1 + i * h2) % m)
            if (!bitSet.get(index)) return false
        }
        return true
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