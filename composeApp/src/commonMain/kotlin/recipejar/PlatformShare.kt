package recipejar

/**
 * Platform import/export via standard sharing (document picker / share sheet).
 * Desktop stubs return null / no-op (desktop uses JFileChooser instead).
 */
expect fun platformImportHtml(
    onResult: (fileName: String?, htmlUtf8: String?, error: String?) -> Unit,
)

/**
 * Share plain text/HTML content (e.g. export-footer recipe) via the system share sheet.
 */
expect fun platformShareText(fileName: String, content: String, onDone: (() -> Unit)? = null)
