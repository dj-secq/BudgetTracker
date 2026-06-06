package com.example.budgettracker.ui.add

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class ReceiptScannerHelper(private val context: Context) {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    data class ScanResult(
        val amount: Double?,
        val note: String
    )
    
    suspend fun scanReceipt(uri: Uri): ScanResult? {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val result = recognizer.process(image).await()
            
            val fullText = result.text
            
            // Try to find the largest currency-like amount
            val amountRegex = Regex("""(?:\p{Sc}|\$|₱|€|£)?\s*(\d{1,3}(?:[.,]\d{3})*[.,]\d{2})""")
            val matches = amountRegex.findAll(fullText)
            
            var maxAmount = 0.0
            for (match in matches) {
                try {
                    val amountStr = match.groupValues[1].replace(",", "").toDouble()
                    if (amountStr > maxAmount) {
                        maxAmount = amountStr
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors for individual matches
                }
            }
            
            ScanResult(
                amount = if (maxAmount > 0) maxAmount else null,
                note = fullText.take(200) // Keep the first 200 chars as note for context
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
