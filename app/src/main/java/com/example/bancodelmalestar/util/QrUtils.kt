package com.example.bancodelmalestar.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.example.bancodelmalestar.data.model.QrTransferData
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrUtils {
    private val gson = Gson()

    fun generateQrCode(content: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
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
        val json = gson.toJson(data)
        return Base64.encodeToString(json.toByteArray(), Base64.DEFAULT)
    }

    fun decodeQrData(encoded: String): QrTransferData? {
        return try {
            val decodedBytes = Base64.decode(encoded, Base64.DEFAULT)
            val json = String(decodedBytes)
            gson.fromJson(json, QrTransferData::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
