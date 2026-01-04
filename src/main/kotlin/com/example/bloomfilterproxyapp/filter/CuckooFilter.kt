package com.example.bloomfilterproxyapp.filter

import kotlin.math.abs

class CuckooFilter(
    private val bucketCount: Int,
    private val bucketSize: Int = 4,
    private val maxKicks: Int = 500,
    private val fingerprintSizeBits: Int = 8
): MembershipFilter {

    // bucketCount x bucketSize tabelarni raspored fingerprintova
    private val buckets: Array<ByteArray> = Array(bucketCount) { ByteArray(bucketSize) }

    override fun add(url: String) {
        val fp = fingerprint(url)
        if (fp == 0.toByte()) return

        val i1 = index1(url)
        val i2 = altIndex(i1, fp)

        // Pokušaj direktnog ubacivanja u i1 ili i2
        if (insertIntoBucket(i1, fp) || insertIntoBucket(i2, fp)) {
            return
        }

        // Relocation (kuckanje)
        var index = if (Math.random() < 0.5) i1 else i2
        var fpToMove = fp

        repeat(maxKicks) {
            val slot = (0 until bucketSize).random()
            val oldFp = buckets[index][slot]
            buckets[index][slot] = fpToMove

            index = altIndex(index, oldFp)
            fpToMove = oldFp

            if (insertIntoBucket(index, fpToMove)) {
                return
            }
        }

        println("CuckooFilter FULL, failed to insert $url")
    }


    override fun mightContain(url: String): Boolean {
        val fp = fingerprint(url)
        if (fp == 0.toByte()) return false

        val i1 = index1(url)
        val i2 = altIndex(i1, fp)

        return bucketHas(i1, fp) || bucketHas(i2, fp)
    }

    private fun insertIntoBucket(bucketIndex: Int, fp: Byte): Boolean {
        val bucket = buckets[bucketIndex]
        for (i in 0 until bucketSize) {
            if (bucket[i] == 0.toByte()) {
                bucket[i] = fp
                return true
            }
        }
        return false
    }

    private fun bucketHas(bucketIndex: Int, fp: Byte): Boolean {
        val bucket = buckets[bucketIndex]
        for (i in 0 until bucketSize) {
            if (bucket[i] == fp) return true
        }
        return false
    }

    private fun fingerprint(url: String): Byte {
        // osnovni hash
        var h = url.hashCode()
        if (h == 0) h = 1
        // koristimo samo fingerprintSizeBits LSB-ova
        val mask = (1 shl fingerprintSizeBits) - 1
        val fp = h and mask
        // 0 rezervisan kao "prazan" – pomjeri za 1
        return (fp + 1).toByte()
    }

    private fun index1(url: String): Int {
        val h = url.hashCode()
        return abs(h) % bucketCount
    }

    private fun altIndex(i1: Int, fp: Byte): Int {
        // drugi index = i1 XOR hash(fingerprint)
        val h = fp.toInt() * 0x5bd1e995
        val x = i1 xor h
        val idx = abs(x) % bucketCount
        return idx
    }
}
