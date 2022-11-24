package com.slapin.gsp.internal.extension

@Suppress("UNCHECKED_CAST")
fun <T> T?.sneakyNull() = this as T