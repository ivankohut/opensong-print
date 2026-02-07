import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.HtmlHymnbook
import sk.ivankohut.HymnbookSong
import java.util.Map.entry

class HtmlHymnbookTest {

    @Test
    fun `contains given songs and index page`() {
        val song1 = SimpleHymnbookSong("First Song", 1, "file1.html", "content1")
        val song2 = SimpleHymnbookSong("Second Song", 2, "file2.html", "content2")
        val sut = HtmlHymnbook("name", listOf(song2, song1))
        // exercise & verify
        assertThat(sut).containsExactlyInAnyOrder(
            entry(song1.filename, song1.content),
            entry(song2.filename, song2.content),
            entry(
                "index.html", """
                <!DOCTYPE html>
                <html lang="sk">
                <head>
                  <meta charset="utf-8">
                  <title>Spevník „name“</title>
                </head>
                <body>

                <h1>Spevník „name“</h1>
                <ul>
                  <li><a href="file1.html">1 - First Song</a></li>
                  <li><a href="file2.html">2 - Second Song</a></li>
                </ul>

                </body>
                </html>
            """.trimIndent()
            )
        )
    }
}

class SimpleHymnbookSong(
    override val name: String,
    override val number: Int,
    override val filename: String,
    override val content: String
) : HymnbookSong
