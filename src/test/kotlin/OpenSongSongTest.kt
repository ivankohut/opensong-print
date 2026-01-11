import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.OpenSongSong
import sk.ivankohut.Section

class OpenSongSongTest {
    @Test
    fun `Parses XML elements of given OpenSong song to a Song`() {
        val sut = OpenSongSong { elementName ->
            mapOf(
                "title" to "name",
                "hymn_number" to "3",
                "lyrics" to """
                    [V1]
                     v1 line 1
                     v1 line 2||
                     v1 line 3|v1 line 4|v1 line 5
                     v1 line 6
                    [C2]
                     c2 line 1
                     c2 line 2||
                     c2 line 3
                     c2 line 4
                    [B3]
                     b3 line 1
                    [P4]
                     p4 line 1
                    [T5]
                     t5 line 1                    
                """.trimIndent()
            ).get(elementName)!!
        }
        // verify
        assertThat(sut.name).isEqualTo("name")
        assertThat(sut.number).isEqualTo(3)
        assertThat(sut.lyrics).satisfiesExactly(
            { verse ->
                assertSection(
                    verse, "Sloha 1", "v1 line 1\nv1 line 2", "v1 line 3\nv1 line 4\nv1 line 5\nv1 line 6"
                )
            },
            { chorus -> assertSection(chorus, "Refrén 2", "c2 line 1\nc2 line 2", "c2 line 3\nc2 line 4") },
            { bridge -> assertSection(bridge, "Prechod 3", "b3 line 1") },
            { prechorus -> assertSection(prechorus, "Predrefrén 4", "p4 line 1") },
            { tag -> assertSection(tag, "Značka 5", "t5 line 1") })
    }

    fun assertSection(verse: Section, name: String, vararg slides: String) {
        assertThat(verse.name).isEqualTo(name)
        assertThat(verse.slides).containsExactlyElementsOf(slides.toList())
    }
}