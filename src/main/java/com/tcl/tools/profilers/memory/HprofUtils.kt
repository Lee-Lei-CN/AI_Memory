package com.tcl.tools.profilers.memory

import com.android.tools.idea.protobuf.ByteString
import com.google.common.hash.Hashing
import com.intellij.openapi.util.Pair
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object HprofUtils {
    fun computeImportedFileStartEndTimestampsNs(file: File): Pair<Long, Long> {
        var hash = Hashing.sha256().hashString(file.absolutePath, StandardCharsets.UTF_8).asLong()
        // Avoid Long.MAX_VALUE which as the end timestamp means ongoing in transport pipeline.
        if (hash == Long.MAX_VALUE || hash == Long.MIN_VALUE || hash == Long.MIN_VALUE + 1) {
            hash /= 2
        }
        // Avoid negative values.
        if (hash < 0) {
            hash = -hash
        }
        val rangeNs = TimeUnit.MICROSECONDS.toNanos(1)
        // Make sure (hash + rangeNs) as the end timestamp doesn't overflow.
        if (hash >= Long.MAX_VALUE - rangeNs) {
            hash -= rangeNs
        }
        return Pair(hash, hash + rangeNs)
    }

    fun buildByteString(file: File) =  ByteBuffer.wrap(readFile(file))

    fun readFile(file: File) = Files.readAllBytes(Paths.get(file.path))
}