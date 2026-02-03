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
        })
        // verify
        assertThat(sut.toString()).isEqualTo(
            """
            <!DOCTYPE html>
            <html lang="sk">
            <head>
              <meta charset="utf-8">
              <title>name</title>
              <style>
                html {
                  margin: 20px;
                }
            
                input[type=checkbox]:checked ~ * .accordable p {
                  margin: 3ex 0 0;
                }
            
                label[for=show-accords] {
                  position: absolute;
                  left: 50px
                }
            
                #show-accords {
                  position: absolute;
                }
            
                @media print {
                  .no-print {
                    display: none !important;
                  }
                }

                #header > * {
                  text-align: right;
                  margin: 0;
                }
            
                p {
                  margin: 0;
                }
            
                .slide {
                  margin-bottom: 0.5em;
                }
            
                .slide:first-child {
                  margin-top: 0.5em;
                }
            
                table {
                  width: 100%;
                  border-collapse: collapse;
                  margin-top: 1em;
                }
            
                td {
                  vertical-align: top;
                  border-top: 1px dotted black;
                }
            
                td:last-child {
                  font-size: x-large;
                  padding-top: 1em;
                  text-align: center;
                }
              </style>
            </head>
            <body>
            
            <label class="no-print" for="show-accords">Zobraziť priestor pre akordy</label>
            <input id="show-accords" class="no-print" type="checkbox" checked>
            <div id="header">
              <h2>č. 123</h2>
              <h3>name</h3>
              <h4>hymnbook</h4>
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