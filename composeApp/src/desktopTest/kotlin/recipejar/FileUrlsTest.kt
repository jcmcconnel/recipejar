package recipejar

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileUrlsTest {

    @Test
    fun normalizeFileUri_expandsJavaStyleToTripleSlash() {
        val javaStyle = "file:/Users/me/recipes/BananaBread.html"
        val norm = FileUrls.normalizeFileUri(javaStyle)
        assertEquals("file:///Users/me/recipes/BananaBread.html", norm)
        assertTrue(norm.startsWith("file:///"))
    }

    @Test
    fun normalizeFileUri_leavesTripleSlashAlone() {
        val ok = "file:///Users/me/x.html"
        assertEquals(ok, FileUrls.normalizeFileUri(ok))
    }

    @Test
    fun fromFile_producesTripleSlashAbsoluteUrl() {
        val f = File("/Users/me/recipes/Test.html")
        val url = FileUrls.fromFile(f)
        assertTrue(url.startsWith("file:///"), url)
        assertTrue(url.contains("Test.html"), url)
    }

    @Test
    fun resolveLoadUrl_prefersExistingDiskFileWhenNoHtmlContent() {
        val dir = Files.createTempDirectory("rj-preview").toFile()
        try {
            val recipe = File(dir, "Pancakes.html").also {
                it.writeText("<html><body>disk content</body></html>", Charsets.UTF_8)
            }
            val fileUrl = FileUrls.fromFile(recipe)
            val previewDir = File(dir, "previews").also { it.mkdirs() }
            val load = resolveLoadUrl(fileUrl, htmlContent = null, previewDir = previewDir)
            assertTrue(load.startsWith("file:///"), load)
            // Must point at the real recipe file so CEF can render it.
            assertTrue(load.contains("Pancakes.html"), load)
            assertEquals(FileUrls.fromFile(recipe), load)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun resolveLoadUrl_writesPreviewWhenOnlyHtmlContent() {
        val dir = Files.createTempDirectory("rj-preview2").toFile()
        try {
            val program = """
                <html><head><title>P</title></head>
                <body><div id="program-footer">Categories: Breakfast</div></body></html>
            """.trimIndent()
            val previewDir = File(dir, "previews").also { it.mkdirs() }
            val load = resolveLoadUrl(fileUrl = "", htmlContent = program, previewDir = previewDir)
            assertTrue(load.startsWith("file:///"), load)
            val previewFile = FileUrls.toFileOrNull(load)!!
            assertTrue(previewFile.isFile, previewFile.absolutePath)
            val body = previewFile.readText(Charsets.UTF_8)
            assertTrue(body.contains("program-footer"), body)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun injectBaseHref_insertsIntoHead() {
        val html = "<html><head><title>T</title></head><body>x</body></html>"
        val out = injectBaseHref(html, "file:///repo/")
        assertTrue(out.contains("""<base href="file:///repo/"/>"""), out)
        assertTrue(out.indexOf("<base") < out.indexOf("</head>"), out)
    }
}
