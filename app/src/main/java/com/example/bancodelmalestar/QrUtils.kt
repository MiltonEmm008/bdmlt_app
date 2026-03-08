package com.example.bancodelmalestar

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.gson.Gson

object QrUtils {
    fun generateQrCode(text: String, size: Int = 512): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun encodeQrData(data: QrTransferData): String {
        return Gson().toJson(data)
    }

    fun decodeQrData(json: String): QrTransferData? {
        return try {
            Gson().fromJson(json, QrTransferData::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
