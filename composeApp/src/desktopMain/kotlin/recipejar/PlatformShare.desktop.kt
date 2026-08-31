package recipejar

/** Desktop uses JFileChooser in Main; these are no-ops for the shared mobile host. */
actual fun platformImportHtml(
    onResult: (fileName: String?, htmlUtf8: String?, error: String?) -> Unit,
) {
    onResult(null, null, "Use Recipe → Import on Desktop with a file chooser")
}

actual fun platformShareText(fileName: String, content: String, onDone: (() -> Unit)?) {
    onDone?.invoke()
}
