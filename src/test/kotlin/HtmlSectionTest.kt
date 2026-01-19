import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import sk.ivankohut.HtmlSection
import sk.ivankohut.Section

class HtmlSectionTest {
    @Test
    fun `consists of table row with lyrics and name cells`() {
        val sut = HtmlSection(SimpleSection("Sloha 1", listOf("line1\nline2", "line3\nline4")))
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
                <td>Sloha 1</td>
              </tr>
""".trimIndent()
        )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Refrén",
            "Refrén 1",
            "Refrén 2",
            "Predrefrén",
            "Predrefrén 1",
            "Predrefrén 2",
            "Prechod",
            "Prechod 1",
            "Prechod 2",
            "Sloha",
            "Sloha 1"]
    )
    fun `is accordable when it is not second verse or later`(name: String) {
        val sut = HtmlSection(SimpleSection(name, listOf()))
        // exercise
        val actual = sut.toString()
        // verify
        assertThat(actual.trimIndent()).isEqualTo(
            """
              <tr>
                <td class="accordable">
                </td>
                <td>${name}</td>
              </tr>
""".trimIndent()
        )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Sloha 2",
            "Sloha 3",
            "Sloha 4"
        ]
    )
    fun `is not accordable when it is second verse or later`(name: String) {
        val sut = HtmlSection(SimpleSection(name, listOf()))
        // exercise
        val actual = sut.toString()
        // verify
        assertThat(actual.trimIndent()).isEqualTo(
            """
              <tr>
                <td>
                </td>
                <td>${name}</td>
              </tr>
""".trimIndent()
        )
    }
}

class SimpleSection(override val name: String, override val slides: Iterable<String>) : Section
