package sk.ivankohut

import org.w3c.dom.Element
import java.io.File
import java.lang.Integer.parseInt
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
    override val name: String = song.name()
    override val number: Int = song.number()
    override val filename: String = "$filename.html"
    override val content: String = content.toString()
}

class HtmlHymnbook(private val name: String, private val htmlSongs: Iterable<HymnbookSong>) :
    Iterable<Map.Entry<String, String>> {
    constructor(openSongSongs: Iterable<Map.Entry<String, String>>, name: String)
            : this(name, openSongSongs.map { entry ->
        val song =
            OpenSongSong(OpenSongXmlElementsContents(entry.value), { code, lyrics -> PhysicalSection(code, lyrics) })
        HtmlHymnbookSong(song, entry.key, HtmlSong(name, song))
    })

    override fun iterator(): Iterator<Map.Entry<String, String>> {
        val listItems = htmlSongs.sortedBy { it.number }
            .joinToString("\n") { "  <li><a href=\"${it.filename}\">${it.number} - ${it.name}</a></li>" }
        val title = "Spevník „${name}“"
        return htmlSongs
            .map { song -> entry(song.filename, song.content) }
            .plus(
                entry(
                    "index.html", """
<!DOCTYPE html>
<html lang="sk">
<head>
  <meta charset="utf-8">
  <title>${title}</title>
</head>
<body>

<h1>${title}</h1>
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

fun interface SectionMapping {
    fun map(code: String, lyrics: String): Iterable<Section>
}

class OpenSongSong(
    private val elements: Function<String, String>,
    private val sectionMapping: SectionMapping
) : Song {
    override fun name(): String = elements.apply("title")
    override fun number(): Int = elements.apply("hymn_number").toInt()
    override fun lyrics(): Iterable<Section> =
        elements.apply("lyrics")
            .split(Regex("(?m)^(?=\\[(\\w+)])"))
            .drop(1)
            .flatMap {
                val result = Regex("\\[(\\w+)]\\n((.*\\n)*(.*))").matchEntire(it)!!
                sectionMapping.map(result.groupValues[1], result.groupValues[2].trimEnd())
            }
}

class PhysicalSection(
    private val sectionCode: String,
    private val lyrics: String,
    private val sectionTypes: Map<Char, Section.Type>
) : Iterable<Section> {

    constructor(sectionCode: String, lyrics: String) : this(
        sectionCode, lyrics, mapOf(
            'C' to Section.Type.CHORUS,
            'V' to Section.Type.VERSE,
            'P' to Section.Type.PRECHORUS,
            'B' to Section.Type.BRIDGE,
            'T' to Section.Type.TAG
        )
    )

    override fun iterator(): Iterator<Section> {
        return lyrics
            .trimEnd()
            .split("\n").map { it.trimEnd() }
            .groupBy { s -> s[0] }
            .filter { it.key != '.' }
            .map {
                object : Section {
                    private val code = sectionCode + (if (it.key == ' ') "" else it.key)
                    override val type: Section.Type =
                        requireNotNull(sectionTypes[code[0]]) { "Unknown section type code: '${code[0]}'" }
                    override val number: Int? = (if (code.length > 1) parseInt(code[1] + "") else null)
                    override val slides: Iterable<String> = it.value
                        .map { it.drop(1).trim().replace("_", "").replace(Regex("  +"), " ") }
                        .joinToString("\n")
                        .split(Regex("(?m)\\s*\\|\\|\\s*"))
                        .map { s -> s.replace(Regex("\\s*\\|\\s*"), "\n") }
                } as Section
            }.iterator()
    }
}

interface Section {
    enum class Type {
        VERSE, CHORUS, PRECHORUS, BRIDGE, TAG
    }

    val type: Type
    val number: Int?
    val slides: Iterable<String>
}

interface Song {
    fun name(): String
    fun number(): Int
    fun lyrics(): Iterable<Section>
}

class HtmlSong(private val hymnbook: String, private val song: Song) {
    override fun toString(): String {
        val lyrics = "<table>\n" +
                song.lyrics().joinToString("") { section -> HtmlSection(section).toString() } +
                "</table>"
        return """
<!DOCTYPE html>
<html lang="sk">
<head>
  <meta charset="utf-8">
  <title>${song.name()}</title>
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
    <h2>č. ${song.number()}</h2>
    <h3>${song.name()}</h3>
    <h4>${hymnbook}</h4>
  </div>
</div>
${lyrics}

</body>
</html>
""".trimIndent()
    }
}

class HtmlSection(private val section: Section, private val sectionNames: Map<Section.Type, String>) {

    constructor(section: Section) : this(
        section, mapOf(
            Section.Type.CHORUS to "Refrén",
            Section.Type.VERSE to "Sloha",
            Section.Type.PRECHORUS to "Predrefrén",
            Section.Type.BRIDGE to "Prechod",
            Section.Type.TAG to "Značka"
        )
    )

    override fun toString(): String {
        val number = section.number
        val accordable =
            if (section.type == Section.Type.VERSE && number != null && number > 1) "" else " class=\"accordable\""
        val name = requireNotNull(sectionNames[section.type]) { "Unsupported section type: '${section.type}'" } +
                (if (number != null) " $number" else "")
        return "  <tr>\n    <td$accordable>\n" + section.slides.map { slide ->
            "      <div class=\"slide\">\n" + slide.split(Regex("\\n"))
                .map { line -> "        <p>${line}</p>\n" }
                .joinToString("") + "      </div>\n"
        }.joinToString("") +
                "    </td>\n    <td>${name}</td>\n  </tr>\n"
    }
}

class FolderOfUtf8Files(private val path: Path, private val files: Iterable<Map.Entry<String, String>>) {
    fun write() {
        path.createDirectories()
        files.forEach { entry -> path.resolve(entry.key).writeBytes(entry.value.toByteArray()) }
    }
}
