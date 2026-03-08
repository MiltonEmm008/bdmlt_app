package com.example.bancodelmalestar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ExportUtils {

    fun generateMovementsPdf(
        context: Context,
        user: User,
        accounts: List<Account>,
        movements: List<Movement>
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        
        var yPos = 40f
        
        // Logo
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_banco)
        val scaledLogo = Bitmap.createScaledBitmap(logo, 50, 50, true)
        canvas.drawBitmap(scaledLogo, 40f, yPos, paint)
        
        // Header
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Banco Del Malestar", 100f, yPos + 30f, paint)
        
        yPos += 70f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("Estado de Cuenta", 40f, yPos, paint)
        
        // User Info
        yPos += 25f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Cliente: ${user.nombre}", 40f, yPos, paint)
        yPos += 15f
        canvas.drawText("Email: ${user.email}", 40f, yPos, paint)
        
        // Accounts Info
        yPos += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Resumen de Cuentas:", 40f, yPos, paint)
        paint.isFakeBoldText = false
        accounts.forEach { account ->
            yPos += 15f
            val type = if (account.tipo == "debito") "Débito" else "Crédito"
            val balance = if (account.tipo == "debito") account.saldo else account.deuda
            canvas.drawText("$type: ${account.numero} - Saldo: $${String.format(Locale.US, "%.2f", balance)}", 50f, yPos, paint)
        }
        
        // Movements Table Header
        yPos += 40f
        paint.isFakeBoldText = true
        canvas.drawText("Últimos Movimientos:", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("Fecha", 40f, yPos, paint)
        canvas.drawText("Descripción", 120f, yPos, paint)
        canvas.drawText("Monto", 480f, yPos, paint)
        
        canvas.drawLine(40f, yPos + 5f, 550f, yPos + 5f, paint)
        
        // Movements
        paint.isFakeBoldText = false
        yPos += 20f
        movements.take(30).forEach { movement ->
            if (yPos > 800) { // Simple page break check
                pdfDocument.finishPage(page)
                // In a real app, you'd start a new page here
                return@forEach 
            }
            canvas.drawText(movement.creadaEn.split("T")[0], 40f, yPos, paint)
            val desc = if (movement.descripcion.length > 45) movement.descripcion.take(42) + "..." else movement.descripcion
            canvas.drawText(desc, 120f, yPos, paint)
            
            val amountText = "$${String.format(Locale.US, "%.2f", movement.monto)}"
            val amountWidth = paint.measureText(amountText)
            canvas.drawText(amountText, 550f - amountWidth, yPos, paint)
            
            yPos += 15f
        }
        
        pdfDocument.finishPage(page)
        
        val file = File(context.cacheDir, "Estado_Cuenta_${user.nombre.replace(" ", "_")}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            shareFile(context, file, "application/pdf")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    fun generateMovementsCsv(context: Context, user: User, movements: List<Movement>) {
        val fileName = "Movimientos_${user.nombre.replace(" ", "_")}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = FileOutputStream(file).bufferedWriter()
            writer.write("ID,Fecha,Tipo,Monto,Descripcion,Estado\n")
            movements.forEach { m ->
                val line = "${m.id},${m.creadaEn},${m.tipo},${m.monto},\"${m.descripcion.replace("\"", "'")}\",${m.estado}\n"
                writer.write(line)
            }
            writer.close()
            shareFile(context, file, "text/csv")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir archivo"))
    }
}
