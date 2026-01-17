package sk.ivankohut

import org.w3c.dom.Element
import java.io.File
import java.nio.file.Path
import java.util.Map.entry
import java.util.function.Function
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes

fun main(vararg args: String) {
    FolderOfUtf8Files(
        Path.of("${args[1]}/html"),
        HtmlHymnbook(
            DirFiles(Path.of(args[1]))
                .filter { it.value.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<song>") },
            args[0]
        )
    ).write()
}

class DirFiles(private val path: Path) : Iterable<Map.Entry<String, String>> {
    override fun iterator(): Iterator<Map.Entry<String, String>> {
        return (File(path.toString()).listFiles() ?: emptyArray())
            .filter { it.isFile }
            .map { file -> entry(file.name, file.readText()) }
            .iterator()
    }
}

interface HymnbookSong {
    val name: String
    val number: Int
    val filename: String
    val content: String
}

class HtmlHymnbookSong(song: Song, filename: String, content: Any) : HymnbookSong {
    override val name: String = song.name
    override val number: Int = song.number
    override val filename: String = "$filename.html"
    override val content: String = content.toString()
}

class HtmlHymnbook(private val name: String, private val htmlSongs: Iterable<HymnbookSong>) :
    Iterable<Map.Entry<String, String>> {
    constructor(openSongSongs: Iterable<Map.Entry<String, String>>, name: String)
            : this(name, openSongSongs.map { entry ->
        val song = OpenSongSong(OpenSongXmlElementsContents(entry.value))
        HtmlHymnbookSong(song, entry.key, HtmlSong(name, song))
    })

    override fun iterator(): Iterator<Map.Entry<String, String>> {
        val listItems = htmlSongs.sortedBy { it.number }
            .joinToString("\n") { "  <li><a href=\"${it.filename}\">${it.number} - ${it.name}</a></li>" }
        return htmlSongs
            .map { song -> entry(song.filename, song.content) }
            .plus(
                entry(
                    "index.html", """
<!DOCTYPE html>
<html lang="sk">
<head>
  <meta charset="utf-8">
  <title>Spevník „${name}“</title>
</head>
<body>

<h1>Spevník „${name}“</h1>
<ul>
${listItems}
</ul>

</body>
</html>
""".trimIndent()
                )
            )
            .iterator()
    }
}

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
        val lyrics = "<table>\n" + song.lyrics.map { section ->
            "  <tr>\n    <td>\n" + section.slides.map { slide ->
                "      <div class=\"slide\">\n" + slide.split(Regex("\\n"))
                    .map { line -> "        <p>${line}</p>\n" }
                    .joinToString("") + "      </div>\n"
            }.joinToString("") +
            "    </td>\n    <td>${section.name}</td>\n  </tr>\n"
        }.joinToString("") + "</table>"
        return """
<!DOCTYPE html>
<html lang="sk">
<head>
  <meta charset="utf-8">
  <title>${song.name}</title>
  <style>
    html {
      margin: 20px;
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

<div id="header">
  <h2>č. ${song.number}</h2>
  <h3>${song.name}</h3>
  <h4>${hymnbook}</h4>
</div>
${lyrics}

</body>
</html>
""".trimIndent()
    }
}

class FolderOfUtf8Files(private val path: Path, private val files: Iterable<Map.Entry<String, String>>) {
    fun write() {
        path.createDirectories()
        files.forEach { entry -> path.resolve(entry.key).writeBytes(entry.value.toByteArray()) }
    }
}
