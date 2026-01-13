import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.HtmlHymnbookSong
import sk.ivankohut.Section
import sk.ivankohut.Song

class HtmlHymnbookSongTest {

    @Test
    fun `retrieves name and number from given song`() {
        val song = createSong()
        val sut = HtmlHymnbookSong(song, "filename", "content")
        // exercise & verify
        assertThat(sut.name).isEqualTo(song.name)
        assertThat(sut.number).isEqualTo(song.number)
    }

    @Test
    fun `adds html extension to given filename`() {
        val sut = HtmlHymnbookSong(createSong(), "filename", "content")
        // exercise
        val actual = sut.filename
        // verify
        assertThat(actual).isEqualTo("filename.html")
    }

    @Test
    fun `retrieves string representation of given content`() {
        val content = "content"
        val sut = HtmlHymnbookSong(createSong(), "filename", content)
        // exercise
        val actual = sut.content
        // verify
        assertThat(actual).isEqualTo(content)
    }

    private fun createSong(): Song {
        return object : Song {
            override val name: String = "name"
            override val number: Int = 123
            override val lyrics: Iterable<Section> = emptyList()
        }
    }
}