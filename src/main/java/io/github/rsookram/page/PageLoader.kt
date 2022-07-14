package io.github.rsookram.page

import android.util.LruCache
import io.github.rsookram.ssr.BgDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.ByteString.Companion.toByteString
import okio.buffer
import okio.source
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageLoader @Inject constructor(@BgDispatcher private val bgDispatcher: CoroutineDispatcher) {

    private val cache: LruCache<File, List<Page>> = LruCache(2)

    suspend fun load(file: File): List<Page> = withContext(bgDispatcher) {
        val cached = cache.get(file)
        if (cached != null) {
            return@withContext cached
        }

        val pages = loadCentralDirectory(file)

        cache.put(file, pages)

        pages
    }

    private fun loadCentralDirectory(file: File): List<Page> {
        val eocd = loadEocd(file)

        val buffer = Buffer()

        val bytes = file
            .runCatching { inputStream() }
            .getOrThrow()
            .apply { fullSkip(eocd.centralDirectoryStartOffset) }
            .source()
            .buffer()
            .readByteArray(eocd.sizeOfCentralDirectoryBytes.toLong())
        buffer.write(bytes)

        val entries = mutableListOf<Pair<String, Int>>()

        while (entries.size < eocd.totalEntries && !buffer.exhausted()) {
            // Skip the following values (offset, length, description)
            //  0 	4 	Central directory file header signature = 0x02014b50
            //  4 	2 	Version made by
            //  6 	2 	Version needed to extract (minimum)
            //  8 	2 	General purpose bit flag
            //  10 	2 	Compression method
            //  12 	2 	File last modification time
            //  14 	2 	File last modification date
            //  16 	4 	CRC-32 of uncompressed data
            //  20 	4 	Compressed size
            //  24 	4 	Uncompressed size
            buffer.skip(28)

            val fileNameLength = buffer.readShortLe()
            val extraFieldLength = buffer.readShortLe()
            val commentLength = buffer.readShortLe()

            // Skip the following values
            // 34	2	Disk number where file starts
            // 36	2	Internal file attributes
            // 38	4	External file attributes
            buffer.skip(8)

            val localFileHeaderOffset = buffer.readIntLe()

            val fileName = buffer.readUtf8(fileNameLength.toLong())

            entries.add(fileName to localFileHeaderOffset)

            buffer.skip((extraFieldLength + commentLength).toLong())
        }

        entries.sortBy { it.first }

        return entries.map { Page(file, it.second) }
    }

    private fun loadEocd(file: File): Eocd {
        val length = file.length()

        val eocdLength = 22 // length excluding comment
        require(length > eocdLength) { "file is too small to be a zip: length=$length" }

        val maxCommentLength = 65535

        val bytes = file
            .runCatching { inputStream() }
            .getOrThrow()
            .apply { fullSkip((length.toInt() - maxCommentLength - eocdLength).coerceAtLeast(0)) }
            .source()
            .buffer()
            .readByteArray()

        val startOfEocd = ((bytes.size - eocdLength) downTo 0)
            .find { bytes.isStartOfEocd(it) }
        requireNotNull(startOfEocd) { "not a valid zip, no EOCD" }

        val eocdBuffer = bytes.toByteString(offset = startOfEocd, byteCount = eocdLength)
            .asByteBuffer()
            .order(ByteOrder.LITTLE_ENDIAN)

        val eocd = Eocd.from(eocdBuffer)

        require(eocd.diskNumber.toInt() == 0)
        require(eocd.diskWithCentralDirectory.toInt() == 0)
        require(eocd.numEntriesOnDisk == eocd.totalEntries) { "no support for spanning archives" }

        return eocd
    }

    private fun ByteArray.isStartOfEocd(i: Int): Boolean {
        if (i + 3 >= size) {
            return false
        }

        return get(i) == (0x50).toByte() &&
                get(i + 1) == (0x4b).toByte() &&
                get(i + 2) == (0x05).toByte() &&
                get(i + 3) == (0x06).toByte()
    }
}

data class Eocd(
    val diskNumber: Short,
    val diskWithCentralDirectory: Short,
    val numEntriesOnDisk: Short,
    val totalEntries: Short,
    val sizeOfCentralDirectoryBytes: Int,
    val centralDirectoryStartOffset: Int,
) {

    companion object {

        fun from(byteBuffer: ByteBuffer) =
            Eocd(
                diskNumber = byteBuffer.getShort(4),
                diskWithCentralDirectory = byteBuffer.getShort(6),
                numEntriesOnDisk = byteBuffer.getShort(8),
                totalEntries = byteBuffer.getShort(10),
                sizeOfCentralDirectoryBytes = byteBuffer.getInt(12),
                centralDirectoryStartOffset = byteBuffer.getInt(16),
            )
    }
}
