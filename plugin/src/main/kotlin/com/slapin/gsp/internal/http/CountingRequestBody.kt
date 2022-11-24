package com.slapin.gsp.internal.http

import java.io.File
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

internal fun File.asCountingRequestBody(
  onProgressUpdate: (written: Long, total: Long) -> Unit,
): RequestBody = CountingRequestBody(this, onProgressUpdate)

private const val BufferSize = 2048L

private class CountingRequestBody(
  private val file: File,
  private val onProgressUpdate: (written: Long, total: Long) -> Unit,
) : RequestBody() {

  private val fileLength = file.length()

  override fun contentType(): MediaType = "application/octet-stream".toMediaType()

  override fun writeTo(sink: BufferedSink) {
    file.source().use { source ->
      var total = 0L
      var read: Long
      while (source.read(sink.buffer, BufferSize).also { read = it } != -1L) {
        total += read
        sink.flush()
        onProgressUpdate.invoke(total, fileLength)
      }
    }
  }
}
