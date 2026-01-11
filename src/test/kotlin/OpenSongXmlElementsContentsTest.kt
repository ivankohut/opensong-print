import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.OpenSongXmlElementsContents

class OpenSongXmlElementsContentsTest {

    @Test
    fun `extracts text content of child elements of the root element of given XML string`() {
        val sut = OpenSongXmlElementsContents(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <song>
              <title>name</title>
              <hymn_number>3</hymn_number>
              <presentation>V1 C V2 C V3 C</presentation>
              <lyrics>[V1]
             v1 line 1
             v1 line 2||
             v1 line 3
             v1 line 4
            [C]
             c line 1
             c line 2||
             c line 3
             c line 4</lyrics>
            </song>""".trimIndent()
        )
        // verify
        assertThat(sut.apply("title")).isEqualTo("name")
        assertThat(sut.apply("hymn_number")).isEqualTo("3")
        assertThat(sut.apply("presentation")).isEqualTo("V1 C V2 C V3 C")
        assertThat(sut.apply("lyrics")).isEqualTo(
            """
            [V1]
             v1 line 1
             v1 line 2||
             v1 line 3
             v1 line 4
            [C]
             c line 1
             c line 2||
             c line 3
             c line 4
        """.trimIndent()
        )
    }
}