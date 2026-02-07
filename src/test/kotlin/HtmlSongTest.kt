import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.HtmlSong
import sk.ivankohut.Song
import sk.ivankohut.Section

class HtmlSongTest {
    @Test
    fun `Exports given song to HTML document`() {
        val sut = HtmlSong("hymnbook", object : Song {
            override fun name(): String = "name"
            override fun number(): Int = 123
            override fun lyrics(): Iterable<Section> = listOf(object : Section {
                override val type: Section.Type = Section.Type.VERSE
                override val number: Int = 1
                override val slides: Iterable<String> = listOf("line1\nline2", "line3\nline4")
            })
        }, "css\n", "configurationDiv\n")
        // verify
        assertThat(sut.toString()).isEqualTo(
            """
            <!DOCTYPE html>
            <html lang="sk">
            <head>
              <meta charset="utf-8">
              <title>name</title>
              <style>
            css
              </style>
            </head>
            <body>
            
            <div id="header">
            configurationDiv
              <div id="song-coordinates">
                <h2>č. 123</h2>
                <h3>name</h3>
                <h4>hymnbook</h4>
              </div>
            </div>
            <table>
              <tr>
                <td class="accordable">
                  <div class="slide">
                    <p>line1</p>
                    <p>line2</p>
                  </div>
                  <div class="slide">
                    <p>line3</p>
                    <p>line4</p>
                  </div>
                </td>
                <td>Sloha 1</td>
              </tr>
            </table>

            </body>
            </html>
        """.trimIndent()
        )
    }
}