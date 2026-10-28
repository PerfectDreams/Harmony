package net.perfectdreams.harmony.wasm

import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferLike
import js.buffer.toArrayBuffer
import js.buffer.toByteArray
import js.typedarrays.Float32Array
import js.typedarrays.Int32Array

// On Kotlin/JS, ByteArray is an Int8Array and FloatArray is a Float32Array under the hood, so there's no JS <-> Wasm boundary to cross and we can use native typed array copies directly
// (unlike Kotlin/Wasm, where we need to bounce through linear memory)

// Int8Array(buffer) + Int8Array.from(...) = native copy
actual fun ArrayBufferLike.toByteArrayFast(): ByteArray = this.toByteArray()

// Int8Array(int8Array).buffer = native copy
actual fun ByteArray.toArrayBufferFast(): ArrayBuffer = this.toArrayBuffer()

// Slice without any parameters = copy
actual fun FloatArray.toFloat32ArrayFast(): Float32Array<ArrayBufferLike> = this.unsafeCast<Float32Array<ArrayBuffer>>().slice().unsafeCast<Float32Array<ArrayBufferLike>>()

// Slice without any parameters = copy
actual fun IntArray.toInt32ArrayFast(): Int32Array<ArrayBufferLike> = this.unsafeCast<Int32Array<ArrayBuffer>>().slice().unsafeCast<Int32Array<ArrayBufferLike>>()
