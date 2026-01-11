import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.HtmlSong
import sk.ivankohut.Song
import sk.ivankohut.Section

class HtmlSongTest {
    @Test
    fun `Exports given song to HTML document`() {
        val sut = HtmlSong("hymnbook", object : Song {
            override val name: String = "name"
            override val number: Int = 123
            override val lyrics: Iterable<Section> = listOf(object : Section {
                override val name: String = "Sloha 1"
                override val slides: Iterable<String> = listOf("line1\nline2", "line3\nline4")
            })
        })
        // verify
        assertThat(sut.toString()).isEqualTo(
            """
            <!DOCTYPE>
            <html lang="sk">
            <head>
              <meta charset="utf-8">
              <title>name</title>
              <style>
                  p {
                      margin: 0;
                  }
                  .slide {
                      margin-bottom: 1em;
                  }
              </style>
            </head>
            <body>
            
            <h1>name</h1>
            <h3>hymnbook, č. 123</h3>
            <h2>Sloha 1</h2>
            <div class="slide">
              <p>line1</p>
              <p>line2</p>
            </div>
            <div class="slide">
              <p>line3</p>
              <p>line4</p>
            </div>

            </body>
            </html>
        """.trimIndent()
        )
    }
}