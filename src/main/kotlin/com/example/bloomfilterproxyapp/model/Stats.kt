package com.example.bloomfilterproxyapp.model

data class Stats(
    var tp: Long = 0,
    var fp: Long = 0,
    var tn: Long = 0,
    var fn: Long = 0,
    var insertTimeMs: Long = 0,
    var lookupTimeMs: Long = 0
) {
    val falsePositiveRate: Double
        get() = if (fp + tn == 0L) 0.0 else fp.toDouble() / (fp + tn)
    val falseNegativeRate: Double
        get() = if (fn + tp == 0L) 0.0 else fn.toDouble() / (fn + tp)
}

