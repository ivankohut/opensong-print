import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.HtmlAllSongs
import sk.ivankohut.Section
import sk.ivankohut.Song

class HtmlAllSongsTest {

    @Test
    fun `contains songs sorted by number with headings and lyrics`() {
        val sut = HtmlAllSongs(
            "name", listOf(
                object : Song {
                    override fun name(): String = "Second Song"
                    override fun number(): Int = 2
                    override fun lyrics(): Iterable<Section> = listOf(object : Section {
                        override val type: Section.Type = Section.Type.CHORUS
                        override val number: Int? = null
                        override val slides: Iterable<String> = listOf("line5\nline6", "line7\nline8")
                    })
                },
                object : Song {
                    override fun name(): String = "First Song"
                    override fun number(): Int = 1
                    override fun lyrics(): Iterable<Section> = listOf(object : Section {
                        override val type: Section.Type = Section.Type.VERSE
                        override val number: Int = 3
                        override val slides: Iterable<String> = listOf("line1\nline2", "line3\nline4")
                    })
                }
            )
        )
        // exercise & verify
        assertThat(sut.toString()).isEqualTo(
            """
            <!DOCTYPE html>
            <html lang="sk">
            <head>
              <meta charset="utf-8">
              <title>Spevník „name“</title>
              <link rel="stylesheet" href="song.css">
            </head>
            <body>

            <h1>Spevník „name“</h1>
            <h2>č. 1 - First Song</h2>
            <h4>Sloha 3</h4>
            <p>line1</p>
            <p>line2</p>
            <p>line3</p>
            <p>line4</p>
            <h2>č. 2 - Second Song</h2>
            <h4>Refrén</h4>
            <p>line5</p>
            <p>line6</p>
            <p>line7</p>
            <p>line8</p>


            </body>
            </html>
        """.trimIndent()
        )
    }
}
