package com.slapin.gsp.internal.retry

import java.time.Duration

internal inline fun withRetry(
  delay: Duration,
  count: Int,
  onFailure: () -> Unit,
  action: () -> Unit,
) {
  var retry: Boolean
  var attempt = 0
  do {
    try {
      action.invoke()
      return
    } catch (e: Exception) {
      Thread.sleep(delay.toMillis())
      retry = count != ++attempt
    }
  } while (retry)
  onFailure.invoke()
}
