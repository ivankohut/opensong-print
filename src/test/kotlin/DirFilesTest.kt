import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.ivankohut.DirFiles
import java.io.File
import java.nio.file.Path
import java.util.Map.entry
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class DirFilesTest {

    @Test
    fun `loads contents of files (ignoring directories) in the given directory`() {
        val path = Path.of("build/tmp/DirFilesTest")
        File(path.toString()).deleteRecursively()
        path.createDirectories()
        path.resolve("file1").writeText("content1")
        path.resolve("file2").writeText("content2")
        path.resolve("dir").createDirectories()
        val sut = DirFiles(path)
        // exercise & verify
        assertThat(sut).containsExactlyInAnyOrder(entry("file1", "content1"), entry("file2", "content2"))
    }
}