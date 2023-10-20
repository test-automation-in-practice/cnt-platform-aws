package example.aws.s3.domain

import example.aws.s3.S3Initializer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType.IMAGE_JPEG_VALUE
import org.springframework.http.MediaType.IMAGE_PNG_VALUE
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import java.util.UUID.randomUUID
import kotlin.annotation.AnnotationTarget.CLASS

@Retention
@Target(CLASS)
@ContextConfiguration(initializers = [S3Initializer::class])
annotation class RunWithDockerizedS3

/**
 * Test that boots the business logic slice of the application and uses LocalStack to also start a locally running S3
 * bucket.
 */
@SpringBootTest
@ComponentScan(basePackageClasses = [BookCoverService::class])
@RunWithDockerizedS3
class BookCoverServiceTest(@Autowired private val cut: BookCoverService) {

    private val id = randomUUID()

    @Test
    fun `returns null when there are no covers`() {
        val result = cut.findCover(id)

        result shouldBe null
    }

    @Test
    fun `returns null when there is no cover for the given id`() {
        val cover = MockMultipartFile(
            id.toString(), "testfile.jpeg", IMAGE_JPEG_VALUE, "123".toByteArray()
        )
        cut.saveCover(id, cover)

        val result = cut.findCover(randomUUID())

        result shouldBe null
    }

    @Test
    fun `correctly finds saved cover`() {
        val cover = MockMultipartFile(
            id.toString(), "testfile.jpeg", IMAGE_JPEG_VALUE, "123".toByteArray()
        )
        cut.saveCover(id, cover)

        val result = cut.findCover(id)

        result!!
        result.contentType shouldBe IMAGE_JPEG_VALUE
        result.contentLength shouldBe 3
        result.byteStream.readAllBytes() shouldBe "123".toByteArray()
    }

    @Test
    fun `correctly updates saved cover`() {
        val cover1 = MockMultipartFile(
            id.toString(), "testfile.jpeg", IMAGE_JPEG_VALUE, "123".toByteArray()
        )
        val cover2 = MockMultipartFile(
            id.toString(), "testfile.png", IMAGE_PNG_VALUE, "45678".toByteArray()
        )
        cut.saveCover(id, cover1)
        cut.saveCover(id, cover2)

        val result = cut.findCover(id)

        result!!
        result.contentType shouldBe IMAGE_PNG_VALUE
        result.contentLength shouldBe 5
        result.byteStream.readAllBytes() shouldBe "45678".toByteArray()
    }

}