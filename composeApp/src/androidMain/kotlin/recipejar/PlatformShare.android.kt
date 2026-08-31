package recipejar

/**
 * Android SAF import/share can be layered later; status-friendly stub for prototype.
 */
actual fun platformImportHtml(
    onResult: (fileName: String?, htmlUtf8: String?, error: String?) -> Unit,
) {
    onResult(null, null, "Import via SAF is not wired yet on Android prototype")
}

actual fun platformShareText(fileName: String, content: String, onDone: (() -> Unit)?) {
    onDone?.invoke()
}
