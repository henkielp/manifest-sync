package com.example.manifestscanner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ---------------------------------------------------------------------------
// Data Classes
// ---------------------------------------------------------------------------

data class ManifestItem(
    val upc: String,
    val description: String,
    val expectedCases: Int,
    val scannedCases: Int = 0
)

data class ExtraItem(
    val barcode: String,
    val scanCount: Int = 1
)

data class ParseResult(
    val items: List<ManifestItem>,
    val upcCount: Int,
    val skippedNineDigitCount: Int,
    val descriptionLineCount: Int,
    val quantityCount: Int
)

// ---------------------------------------------------------------------------
// State Machine
// ---------------------------------------------------------------------------

sealed interface AppState {
    data object Idle : AppState
    data object Capturing : AppState
    data class CaptureReview(val croppedBitmap: Bitmap) : AppState
    data object Parsing : AppState
    data class PagePreview(
        val pendingItems: List<ManifestItem>,
        val existingCount: Int,
        val warning: String? = null
    ) : AppState
    data class ManifestReady(val items: List<ManifestItem>) : AppState
    data class Scanning(
        val items: List<ManifestItem>,
        val extraItems: List<ExtraItem>
    ) : AppState
    data class PendingConfirm(
        val items: List<ManifestItem>,
        val extraItems: List<ExtraItem>,
        val scannedBarcode: String,
        val matchedIndex: Int?,
        val matchedDescription: String
    ) : AppState
    data class Reporting(
        val missingItems: List<ManifestItem>,
        val fullyReceivedItems: List<ManifestItem>,
        val multipleItems: List<ManifestItem>,
        val extraItems: List<ExtraItem>
    ) : AppState
    data class Error(val message: String) : AppState
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class ManifestViewModel : ViewModel() {

    private val _state = MutableStateFlow<AppState>(AppState.Idle)
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var manifestItems: MutableList<ManifestItem> = mutableListOf()
    private var extraItems: MutableList<ExtraItem> = mutableListOf()
    private var appendMode = false
    private var pendingItems: List<ManifestItem> = emptyList()
    private var expectedItemCount = 0

    // -----------------------------------------------------------------------
    // Phase 1a: Capture flow
    // -----------------------------------------------------------------------

    fun startCapture() {
        appendMode = false
        _state.value = AppState.Capturing
    }

    fun captureNextPage() {
        appendMode = true
        _state.value = AppState.Capturing
    }

    fun onPhotoCropped(croppedBitmap: Bitmap) {
        _state.value = AppState.CaptureReview(croppedBitmap = croppedBitmap)
    }

    fun onRetryCapture() {
        val current = _state.value
        if (current is AppState.CaptureReview && !current.croppedBitmap.isRecycled) {
            current.croppedBitmap.recycle()
        }
        _state.value = AppState.Capturing
    }

    fun acceptPage() {
        if (appendMode) {
            val existingUpcs = manifestItems.map { it.upc }.toSet()
            val newItems = pendingItems.filter { it.upc !in existingUpcs }
            manifestItems.addAll(newItems)
        } else {
            manifestItems = pendingItems.toMutableList()
            extraItems.clear()
        }
        appendMode = false
        pendingItems = emptyList()
        _state.value = AppState.ManifestReady(items = manifestItems.toList())
    }

    fun retryPage() {
        pendingItems = emptyList()
        _state.value = AppState.Capturing
    }

    fun cancelCapture() {
        appendMode = false
        if (manifestItems.isNotEmpty()) {
            _state.value = AppState.ManifestReady(items = manifestItems.toList())
        } else {
            _state.value = AppState.Idle
        }
    }

    // -----------------------------------------------------------------------
    // Phase 1b: Ingestion (OCR text to structured manifest)
    // -----------------------------------------------------------------------

    fun onProcessCrop() {
        _state.value = AppState.Parsing
    }

    fun onManifestTextExtracted(rawText: String) {
        if (_state.value !is AppState.Parsing) {
            _state.value = AppState.Parsing
        }

        val result = parseManifestText(rawText)
        val parsed = result.items

        if (parsed.isEmpty()) {
            appendMode = false
            if (manifestItems.isNotEmpty()) {
                pendingItems = emptyList()
                _state.value = AppState.ManifestReady(items = manifestItems.toList())
            } else {
                _state.value = AppState.Error(
                    "Could not parse any items from the captured image. " +
                    "Make sure the UPC, Description, and Cases columns are " +
                    "fully inside the crop guide and well-lit."
                )
            }
            return
        }

        pendingItems = parsed

        val warnings = mutableListOf<String>()
        if (result.skippedNineDigitCount > 0 &&
            result.skippedNineDigitCount != result.upcCount) {
            warnings.add(
                "Found ${result.upcCount} UPCs but ${result.skippedNineDigitCount} Item Nbrs. " +
                "A UPC may have been misread."
            )
        }
        

        _state.value = AppState.PagePreview(
            pendingItems = parsed,
            existingCount = manifestItems.size,
            warning = if (warnings.isNotEmpty()) warnings.joinToString("\n") else null
        )
    }

    // -----------------------------------------------------------------------
    // Item editing
    // -----------------------------------------------------------------------

    fun setExpectedItemCount(count: Int) {
        expectedItemCount = count
        if (manifestItems.isNotEmpty()) {
            _state.value = AppState.ManifestReady(items = manifestItems.toList())
        }
    }

    fun getExpectedItemCount(): Int = expectedItemCount

    fun updateItemQuantity(position: Int, newQuantity: Int) {
        if (position in manifestItems.indices) {
            manifestItems[position] = manifestItems[position].copy(expectedCases = newQuantity)
            _state.value = AppState.ManifestReady(items = manifestItems.toList())
        }
    }

    fun addManifestItemManually(upc: String, description: String, expectedCases: Int) {
        val item = ManifestItem(
            upc = upc.trim(),
            description = description.trim(),
            expectedCases = expectedCases
        )
        manifestItems.add(item)
        _state.value = AppState.ManifestReady(items = manifestItems.toList())
    }

    // -----------------------------------------------------------------------
    // Phase 2: Scanning & Confirmation
    // -----------------------------------------------------------------------

    fun startScanning() {
        _state.value = AppState.Scanning(
            items = manifestItems.toList(),
            extraItems = extraItems.toList()
        )
    }

    fun onBarcodeDetected(scannedBarcode: String) {
        if (_state.value is AppState.PendingConfirm) return

        val cleanBarcode = scannedBarcode.trim()
        if (cleanBarcode.isEmpty()) return

        val processedBarcode = preprocessBarcode(cleanBarcode)
        val matchResult = findMatchingItem(processedBarcode, manifestItems)

        val description = if (matchResult != null) {
            manifestItems[matchResult].description
        } else {
            "NOT ON MANIFEST"
        }

        _state.value = AppState.PendingConfirm(
            items = manifestItems.toList(),
            extraItems = extraItems.toList(),
            scannedBarcode = processedBarcode,
            matchedIndex = matchResult,
            matchedDescription = description
        )
    }

    fun confirmScan() {
        val pending = _state.value as? AppState.PendingConfirm ?: return

        if (pending.matchedIndex != null) {
            val idx = pending.matchedIndex
            val current = manifestItems[idx]
            manifestItems[idx] = current.copy(scannedCases = current.scannedCases + 1)
        } else {
            val existing = extraItems.indexOfFirst { it.barcode == pending.scannedBarcode }
            if (existing >= 0) {
                val current = extraItems[existing]
                extraItems[existing] = current.copy(scanCount = current.scanCount + 1)
            } else {
                extraItems.add(ExtraItem(barcode = pending.scannedBarcode))
            }
        }

        _state.value = AppState.Scanning(
            items = manifestItems.toList(),
            extraItems = extraItems.toList()
        )
    }

    fun rejectScan() {
        _state.value = AppState.Scanning(
            items = manifestItems.toList(),
            extraItems = extraItems.toList()
        )
    }

    // -----------------------------------------------------------------------
    // Phase 3: Discrepancy Report
    // -----------------------------------------------------------------------

    fun generateReport() {
        val missing = manifestItems.filter { it.scannedCases < it.expectedCases }
        val fullyReceived = manifestItems.filter { it.scannedCases == it.expectedCases }
        val multiples = manifestItems.filter { it.scannedCases > it.expectedCases }

        _state.value = AppState.Reporting(
            missingItems = missing,
            fullyReceivedItems = fullyReceived,
            multipleItems = multiples,
            extraItems = extraItems.toList()
        )
    }
    // -----------------------------------------------------------------------
    // Navigation helpers
    // -----------------------------------------------------------------------

    fun returnToScanning() {
        _state.value = AppState.Scanning(
            items = manifestItems.toList(),
            extraItems = extraItems.toList()
        )
    }

    fun reset() {
        val current = _state.value
        if (current is AppState.CaptureReview && !current.croppedBitmap.isRecycled) {
            current.croppedBitmap.recycle()
        }
        manifestItems.clear()
        extraItems.clear()
        expectedItemCount = 0
        _state.value = AppState.Idle
    }

    // -----------------------------------------------------------------------
    // Progress queries
    // -----------------------------------------------------------------------

    fun lastItemSummary(): String? {
        val last = manifestItems.lastOrNull() ?: return null
        val index = manifestItems.size
        return "#$index  ${last.upc}  ${last.description}"
    }

    fun overallProgress(): Float {
        val totalExpected = manifestItems.sumOf { it.expectedCases }
        if (totalExpected == 0) return 0f
        val totalScanned = manifestItems.sumOf { it.scannedCases }
        return totalScanned.toFloat() / totalExpected.toFloat()
    }

    fun outstandingLineCount(): Int =
        manifestItems.count { it.scannedCases < it.expectedCases }

    // -----------------------------------------------------------------------
    // Internal: Barcode Preprocessing (GS1-128 GTIN Extraction)
    // -----------------------------------------------------------------------

    internal fun preprocessBarcode(rawBarcode: String): String {
        var value = rawBarcode.trim()

        if (value.startsWith("]") && value.length > 3) {
            value = value.substring(3)
        }

        if (value.startsWith("01") && value.length >= 16) {
            val gtin14 = value.substring(2, 16)
            if (gtin14.all { it.isDigit() }) {
                return gtin14
            }
        }

        return value
    }

    // -----------------------------------------------------------------------
    // Internal: Substring Matching Engine
    // -----------------------------------------------------------------------

    internal fun findMatchingItem(
        scannedBarcode: String,
        items: List<ManifestItem>
    ): Int? {
        for ((index, item) in items.withIndex()) {
            if (item.upc.isNotEmpty() && scannedBarcode.contains(item.upc)) {
                return index
            }
        }
        return null
    }

    // -----------------------------------------------------------------------
    // Internal: OCR Text Parser (Column-Bucket Strategy)
    // -----------------------------------------------------------------------

    internal fun parseManifestText(rawText: String): ParseResult {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return ParseResult(emptyList(), 0, 0, 0, 0)

        val upcs = mutableListOf<String>()
        val descriptions = mutableListOf<String>()
        val quantities = mutableListOf<Int>()
        var skippedCount = 0
        var phase = 0

        for (line in lines) {
            val stripped = line.replace("\\s".toRegex(), "")
            val isPureDigits = stripped.all { it.isDigit() } && stripped.isNotEmpty()

            when (phase) {
                0 -> {
                    if (isPureDigits) {
                        when {
                            stripped.length >= 6 -> upcs.add(stripped)
                            else -> { skippedCount++ }
                        }
                    } else {
                        phase = 1
                        descriptions.add(line)
                    }
                }

                1 -> {
                    if (isPureDigits && stripped.length in 1..2 && descriptions.isNotEmpty()) {
                        val qty = stripped.toIntOrNull()
                        if (qty != null && qty > 0) {
                            phase = 2
                            quantities.add(qty)
                        } else {
                            descriptions.add(line)
                        }
                    } else {
                        descriptions.add(line)
                    }
                }

                2 -> {
                    if (isPureDigits && stripped.length in 1..4) {
                        val qty = stripped.toIntOrNull()
                        if (qty != null && qty > 0) {
                            quantities.add(qty)
                        }
                    }
                }
            }
        }

        if (upcs.isEmpty()) return ParseResult(emptyList(), 0, skippedCount, descriptions.size, quantities.size)

        val itemCount = upcs.size
        val mergedDescriptions = mergeDescriptions(descriptions, itemCount)
        val finalQuantities = MutableList(itemCount) { 1 }

        val items = upcs.mapIndexed { i, upc ->
            ManifestItem(
                upc = upc,
                description = mergedDescriptions.getOrElse(i) { "Item ${i + 1}" },
                expectedCases = finalQuantities.getOrElse(i) { 1 }
            )
        }

        return ParseResult(
            items = items,
            upcCount = upcs.size,
            skippedNineDigitCount = skippedCount,
            descriptionLineCount = descriptions.size,
            quantityCount = quantities.size
        )
    }

    private fun mergeDescriptions(
        descriptions: List<String>,
        targetCount: Int
    ): List<String> {
        if (descriptions.isEmpty()) {
            return List(targetCount) { "Item ${it + 1}" }
        }
        if (descriptions.size == targetCount) {
            return descriptions
        }
        if (descriptions.size < targetCount) {
            return descriptions + List(targetCount - descriptions.size) {
                "Item ${descriptions.size + it + 1}"
            }
        }

        val xPattern = Regex("[xX✗✕×\\*]\\s*$")
        val xStandalone = Regex("^[xX✗✕×\\*]$")
        val hasDelimiters = descriptions.any {
            xStandalone.matches(it.trim()) || xPattern.containsMatchIn(it.trim())
        }

        if (hasDelimiters) {
            return mergeDescriptionsWithDelimiters(descriptions, targetCount)
        }

        val result = mutableListOf<String>()
        val linesPerItem = descriptions.size.toFloat() / targetCount.toFloat()

        var cursor = 0f
        for (i in 0 until targetCount) {
            val nextCursor = cursor + linesPerItem
            val startIdx = cursor.toInt()
            val endIdx = nextCursor.toInt().coerceAtLeast(startIdx + 1)
                .coerceAtMost(descriptions.size)
            val merged = descriptions.subList(startIdx, endIdx).joinToString(" ")
            result.add(merged)
            cursor = nextCursor
        }

        return result
    }

    private fun mergeDescriptionsWithDelimiters(
        descriptions: List<String>,
        targetCount: Int
    ): List<String> {
        val groups = mutableListOf<MutableList<String>>()
        var currentGroup = mutableListOf<String>()

        val xEnd = Regex("\\s+[xX✗✕×\\*]\\s*$")
        val xAlone = Regex("^[xX✗✕×\\*]$")

        for (line in descriptions) {
            val trimmed = line.trim()
            if (xAlone.matches(trimmed)) {
                if (currentGroup.isNotEmpty()) {
                    groups.add(currentGroup)
                    currentGroup = mutableListOf()
                }
            } else if (xEnd.containsMatchIn(trimmed)) {
                val cleaned = xEnd.replace(trimmed, "").trim()
                if (cleaned.isNotEmpty()) {
                    currentGroup.add(cleaned)
                }
                groups.add(currentGroup)
                currentGroup = mutableListOf()
            } else {
                currentGroup.add(line)
            }
        }
        if (currentGroup.isNotEmpty()) {
            groups.add(currentGroup)
        }

        val merged = groups.map { it.joinToString(" ") }

        return when {
            merged.size == targetCount -> merged
            merged.size < targetCount -> {
                merged + List(targetCount - merged.size) {
                    "Item ${merged.size + it + 1}"
                }
            }
            else -> merged.take(targetCount)
        }
    }
}
