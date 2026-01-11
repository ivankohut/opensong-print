package sk.ivankohut

import org.w3c.dom.Element
import java.util.function.Function
import javax.xml.parsers.DocumentBuilderFactory

fun main(vararg args: String) {
    print(HtmlSong(args[0], OpenSongSong(OpenSongXmlElementsContents(standardInput()))).toString())
}

private fun standardInput(): String = System.`in`.bufferedReader(Charsets.UTF_8).use { it.readText() }

class OpenSongXmlElementsContents(xml: String) : Function<String, String> {
    private val root: Element by lazy {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        builder.parse(xml.byteInputStream()).documentElement
    }

    override fun apply(element: String): String = root.getElementsByTagName(element).item(0).textContent
}

class OpenSongSong(elements: Function<String, String>) : Song {

    override val name: String = elements.apply("title")
    override val number: Int = elements.apply("hymn_number").toInt()
    private val sectionNames = mapOf(
        'C' to "Refrén",
        'V' to "Sloha",
        'P' to "Predrefrén",
        'B' to "Prechod",
        'T' to "Značka"
    )
    override val lyrics: Iterable<Section> =
        Regex("\\[(\\w+)]((\\n .*)+)").findAll(elements.apply("lyrics")).map { result ->
            object : Section {
                private val code = result.groupValues[1]
                override val name: String =
                    sectionNames.getOrDefault(code[0], "" + code[0]) + (if (code.length > 1) " " + code[1] else "")
                override val slides: Iterable<String> = result.groupValues[2]
                    .trim()
                    .split("\n").map { it.trim() }.joinToString("\n")
                    .split(Regex("\\|\\|\\n"))
                    .map { s -> s.replace('|', '\n') }
            }
        }.toList()
}

interface Section {
    val name: String
    val slides: Iterable<String>
}

interface Song {
    val name: String
    val number: Int
    val lyrics: Iterable<Section>
}

class HtmlSong(private val hymnbook: String, private val song: Song) {
    override fun toString(): String {
        val lyrics = song.lyrics.map { section ->
            "<h2>${section.name}</h2>\n" + section.slides.map { slide ->
                "<div class=\"slide\">\n" + slide.split(Regex("\\n"))
                    .map { line -> "  <p>${line}</p>\n" }
                    .joinToString("") + "</div>\n"
            }.joinToString("")
        }.joinToString("")
        return """
<!DOCTYPE>
<html lang="sk">
<head>
  <meta charset="utf-8">
  <title>${song.name}</title>
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

<h1>${song.name}</h1>
<h3>${hymnbook}, č. ${song.number}</h3>
${lyrics}
</body>
</html>
""".trimIndent()
    }
}
