import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import sk.ivankohut.OpenSongSong
import sk.ivankohut.Section
import sk.ivankohut.SectionMapping

class OpenSongSongTest {

    @Test
    fun `parses name, number and physical sections of lyrics`() {
        val sectionMapping = mock(SectionMapping::class.java)
        val section1 = mock(Section::class.java)
        val section2 = mock(Section::class.java)
        Mockito.`when`(sectionMapping.map("V", ".C      Dmi F G\n1Hello, He____llo|World")).thenReturn(listOf(section1))
        Mockito.`when`(sectionMapping.map("C", " chorus1\n chorus2\n chorus3")).thenReturn(listOf(section2))
        // exercise
        val sut = OpenSongSong(
            { elementName ->
                mapOf(
                    "title" to "name",
                    "hymn_number" to "3",
                    "lyrics" to """
                    [V]
                    .C      Dmi F G
                    1Hello, He____llo|World
                    [C]
                     chorus1
                     chorus2
                     chorus3
                """.trimIndent()
                ).get(elementName)!!
            },
            sectionMapping
        )
        // verify
        assertThat(sut.name()).isEqualTo("name")
        assertThat(sut.number()).isEqualTo(3)
        assertThat(sut.lyrics()).containsExactly(section1, section2)
    }
}