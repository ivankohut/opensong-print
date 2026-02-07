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
                  font-family: "Liberation Serif", serif;
                  --selected-font-size: 24px;
                  --space-for-accords: 3ex;
                }
            
                @media print {
                  .no-print {
                    display: none !important;
                  }
                }
            
                #header {
                  display: flex;
                  justify-content: space-between;
                  width: 100%;
                }
            
                #configuration {
                  vertical-align: top;
                }
            
                #song-coordinates {
                  text-align: right;
                }
            
                #song-coordinates > * {
                  text-align: right;
                  margin: 0;
                }
            
                p {
                  margin: 0;
                }
            
                .accordable p {
                  margin: var(--space-for-accords) 0 0;
                }
            
                .slide {
                  margin-bottom: 0.5em;
                  font-size: var(--selected-font-size);
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
                  font-size: larger;
                  padding-top: 1em;
                  text-align: center;
                }
              </style>
            </head>
            <body>
            
            <div id="header">
              <div id="configuration">
                <div>
                  <label class="no-print" for="show-accords">Zobraziť priestor pre akordy</label>
                  <input id="show-accords" class="no-print" type="checkbox" checked>
                  <script>
                    document.getElementById('show-accords').addEventListener('change', function () {
                      document.documentElement.style.setProperty('--space-for-accords', this.checked ? '3ex' : '0');
                    });
                  </script>
                </div>
                <div>
                  <label class="no-print" for="select-font-size">Veľkosť písma</label>
                  <input id="select-font-size" class="no-print" type="number" min="8" max="48" value="24">
                  <script>
                    document.getElementById('select-font-size').addEventListener('change', function () {
                      document.documentElement.style.setProperty('--selected-font-size', this.value + 'px');
                    });
                  </script>
                </div>
              </div>
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