import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import sk.ivankohut.HtmlSection
import sk.ivankohut.Section
import sk.ivankohut.Section.Type

class HtmlSectionTest {
    @Test
    fun `consists of table row with lyrics and name cells`() {
        val sut = HtmlSection(SimpleSection(Type.VERSE, 1, listOf("line1\nline2", "line3\nline4")), mapOf(Type.VERSE to "verse"))
        // verify
        assertThat(sut.toString().trimIndent()).isEqualTo(
            """
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
                <td>verse 1</td>
              </tr>
""".trimIndent()
        )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "CHORUS,",
            "CHORUS, 1",
            "CHORUS, 2",
            "PRECHORUS,",
            "PRECHORUS, 1",
            "PRECHORUS, 2",
            "BRIDGE,",
            "BRIDGE, 1",
            "BRIDGE, 2",
            "VERSE,",
            "VERSE, 1"]
    )
    fun `is accordable when it is not second verse or later`(type: Type, number: Int?) {
        val sut = HtmlSection(SimpleSection(type, number, listOf()))
        // exercise
        val actual = sut.toString()
        // verify
        assertThat(actual.trimIndent()).containsPattern(
            """
              <tr>
                <td class="accordable">
                </td>
                <td>(.*)</td>
              </tr>
""".trimIndent()
        )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "VERSE, 2",
            "VERSE, 3",
            "VERSE, 4"
        ]
    )
    fun `is not accordable when it is second verse or later`(type: Type, number: Int?) {
        val sut = HtmlSection(SimpleSection(type, number, listOf()))
        // exercise
        val actual = sut.toString()
        // verify
        assertThat(actual.trimIndent()).containsPattern(
            """
              <tr>
                <td>
                </td>
                <td>(.*)</td>
              </tr>
""".trimIndent()
        )
    }
}

class SimpleSection(override val type: Section.Type, override val number: Int?, override val slides: Iterable<String>) :
    Section
