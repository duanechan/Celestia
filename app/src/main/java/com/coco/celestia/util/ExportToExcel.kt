package com.coco.celestia.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.UserData
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun exportToExcel (context: Context, transactionData: Map<UserData, List<TransactionData>>) {
    val workbook = XSSFWorkbook()
    val sheet: Sheet = workbook.createSheet("Audit Log")

    val headerRow: Row = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("Date")
    headerRow.createCell(1).setCellValue("Transaction ID")
    headerRow.createCell(2).setCellValue("User Name")
    headerRow.createCell(3).setCellValue("User Role")
    headerRow.createCell(4).setCellValue("Audit Type")
    headerRow.createCell(5).setCellValue("Audit Description")

    var rowIndex = 1

    transactionData.forEach { (user, transactions) ->
        transactions.forEach { transaction ->
            val row: Row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(transaction.date)
            row.createCell(1).setCellValue(transaction.transactionId)
            row.createCell(2).setCellValue("${user.firstname} ${user.lastname}")
            row.createCell(3).setCellValue(user.role)
            row.createCell(4).setCellValue(transaction.type)
            row.createCell(5).setCellValue(transaction.description)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "transactions.xlsx")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/")
        }

        val contentResolver: ContentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        if (uri != null) {
            try {
                val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    workbook.write(outputStream)
                    workbook.close()
                    Toast.makeText(context, "File Downloaded Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to create output stream", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Error writing file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to create file URI", Toast.LENGTH_SHORT).show()
        }
    } else {
        val directory = context.getExternalFilesDir(null)
        if (directory != null) {
            val file = File(directory, "transactions.xlsx")
            try {
                FileOutputStream(file).use { fos ->
                    workbook.write(fos)
                    workbook.close()
                    Toast.makeText(context, "File Downloaded Successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Error writing file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}