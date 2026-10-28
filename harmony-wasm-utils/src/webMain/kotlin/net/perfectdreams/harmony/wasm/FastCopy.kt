package net.perfectdreams.harmony.wasm

import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferLike
import js.typedarrays.Float32Array
import js.typedarrays.Int32Array

expect fun ArrayBufferLike.toByteArrayFast(): ByteArray

expect fun ByteArray.toArrayBufferFast(): ArrayBuffer

expect fun FloatArray.toFloat32ArrayFast(): Float32Array<ArrayBufferLike>

expect fun IntArray.toInt32ArrayFast(): Int32Array<ArrayBufferLike>