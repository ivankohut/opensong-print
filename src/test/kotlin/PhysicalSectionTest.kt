import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.PhysicalSection
import sk.ivankohut.Section

class PhysicalSectionTest {

    @Test
    fun `removes leading char of lyrics' lines`() {
        val sut = createSut(" line1\n line2")
        // verify
        assertSlidesOfSingleSection(sut, "line1\nline2")
    }

    @Test
    fun `removes underscores in lyrics`() {
        val sut = createSut(" l_in___e")
        // verify
        assertSlidesOfSingleSection(sut, "line")
    }

    @Test
    fun `replaces multiple spaces with one space`() {
        val sut = createSut(" l in   e")
        // verify
        assertSlidesOfSingleSection(sut, "l in e")
    }

    @Test
    fun `removes leading and trailing spaces`() {
        val sut = createSut("  line   ")
        // verify
        assertSlidesOfSingleSection(sut, "line")
    }

    @Test
    fun `splits line at pipe removing spaces around pipe`() {
        val sut = createSut(" line1  |   line2")
        // verify
        assertSlidesOfSingleSection(sut, "line1\nline2")
    }

    @Test
    fun `new slide at double pipe removing spaces around pipes`() {
        val sut = createSut(" line1  ||   line2")
        // verify
        assertSlidesOfSingleSection(sut, "line1", "line2")
    }

    @Test
    fun `new slide at double pipe ignoring new line`() {
        val sut = createSut(" line1    ||   \n    line2")
        // verify
        assertSlidesOfSingleSection(sut, "line1", "line2")
    }

    @Test
    fun `all sections of the given physical section`() {
        val sut = PhysicalSection(
            "V", """
            .C      Dmi F G
            1Hello, He____llo|World1
            2Hi,    He____llo|World2
            .A      Bmi E E#
            1Hello, He____llo|World3
            2Hi,    He____llo|World4
        """.trimIndent()
        )
        // exercise & verify
        assertThat(sut).satisfiesExactly(
            { verse ->
                assertVerseSection(
                    verse, 1, "Hello, Hello\nWorld1\nHello, Hello\nWorld3"
                )
            },
            { verse ->
                assertVerseSection(
                    verse, 2, "Hi, Hello\nWorld2\nHi, Hello\nWorld4"
                )
            }
        )
    }
}

private fun createSut(lyrics: String): PhysicalSection = PhysicalSection("C", lyrics)

private fun assertVerseSection(section: Section, number: Int?, vararg slides: String) {
    assertThat(section.type).isEqualTo(Section.Type.VERSE)
    assertThat(section.number).isEqualTo(number)
    assertThat(section.slides).containsExactlyElementsOf(slides.toList())
}

private fun assertSlidesOfSingleSection(section: PhysicalSection, vararg slides: String) {
    assertThat(section).satisfiesExactly(
        { section -> assertThat(section.slides).containsExactly(*slides) })
}
