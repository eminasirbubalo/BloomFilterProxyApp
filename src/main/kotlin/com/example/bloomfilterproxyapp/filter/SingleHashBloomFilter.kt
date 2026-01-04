package com.example.bloomfilterproxyapp.filter

import java.util.BitSet

class SingleHashBloomFilter(
    private val m: Int   // broj bitova
): MembershipFilter {
    private val bitSet = BitSet(m)

    override fun add(url: String) {
        val idx = index(url)
        bitSet.set(idx)
    }

    override fun mightContain(url: String): Boolean {
        val idx = index(url)
        return bitSet.get(idx)
    }

    private fun index(url: String): Int {
        val h = hashFNV1a(url) // ili kombinacija hashCode + FNV
        return kotlin.math.abs(h) % m
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
