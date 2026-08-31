package recipejar

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSURL
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UniformTypeIdentifiers.UTTypeHTML
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject

/**
 * Present iOS document picker for HTML import, or share sheet for export text.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun platformImportHtml(
    onResult: (fileName: String?, htmlUtf8: String?, error: String?) -> Unit,
) {
    val root = rootViewController()
    if (root == null) {
        onResult(null, null, "No view controller for document picker")
        return
    }
    val types = listOfNotNull(UTTypeHTML, UTTypeItem)
    val picker = UIDocumentPickerViewController(forOpeningContentTypes = types)
    picker.allowsMultipleSelection = false
    val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>,
        ) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            if (url == null) {
                onResult(null, null, "No file selected")
                return
            }
            val accessing = url.startAccessingSecurityScopedResource()
            try {
                val path = url.path
                if (path == null) {
                    onResult(null, null, "Invalid file path")
                    return
                }
                val text = NSString.stringWithContentsOfFile(
                    path,
                    encoding = NSUTF8StringEncoding,
                    error = null,
                )
                if (text == null) {
                    onResult(null, null, "Could not read file")
                } else {
                    onResult(url.lastPathComponent, text.toString(), null)
                }
            } finally {
                if (accessing) url.stopAccessingSecurityScopedResource()
            }
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            onResult(null, null, null)
        }
    }
    picker.setDelegate(delegate)
    ImportDelegateHolder.delegate = delegate
    root.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun platformShareText(fileName: String, content: String, onDone: (() -> Unit)?) {
    val root = rootViewController()
    if (root == null) {
        onDone?.invoke()
        return
    }
    val tmp = NSTemporaryDirectory() + fileName
    (content as NSString).writeToFile(
        tmp,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null,
    )
    val url = NSURL.fileURLWithPath(tmp)
    val activity = UIActivityViewController(
        activityItems = listOf(url),
        applicationActivities = null,
    )
    activity.setCompletionWithItemsHandler { _, _, _, _ ->
        onDone?.invoke()
    }
    root.presentViewController(activity, animated = true, completion = null)
}

private object ImportDelegateHolder {
    var delegate: NSObject? = null
}

private fun rootViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val window = app.keyWindow
        ?: (app.windows.firstOrNull() as? UIWindow)
    return window?.rootViewController
}
