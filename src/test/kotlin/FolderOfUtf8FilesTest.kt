import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.FolderOfUtf8Files
import java.io.File
import java.nio.file.Path
import java.util.Map.entry

class FolderOfUtf8FilesTest {
    @Test
    fun `writes contents of given files to filesystem`() {
        val path = Path.of("build/tmp/FolderTest")
        File(path.toString()).deleteRecursively()
        val files = listOf(entry("file1", "content1"), entry("file2", "content2"))
        val sut = FolderOfUtf8Files(path, files)
        // exercise
        sut.write()
        // verify
        files.forEach { file -> assertThat(path.resolve(file.key)).hasBinaryContent(file.value.toByteArray()) }
    }
}