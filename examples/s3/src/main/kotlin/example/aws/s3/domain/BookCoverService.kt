package example.aws.s3.domain

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.ObjectMetadata
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

/**
 * Business class that implements the details of loading and saving book covers. Offers a clean api that hides the
 * lower level details of the S3 sdk.
 */
@Service
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@EnableConfigurationProperties(S3Properties::class)
class BookCoverService(
    private val s3: AmazonS3,
    private val s3Properties: S3Properties,
) {

    fun findCover(id: UUID): BookCoverData? {
        val s3Object = try {
            s3.getObject(s3Properties.bucketName, id.toString())
        } catch (e: AmazonS3Exception) {
            when (e.statusCode) {
                NOT_FOUND.value() -> return null
                else -> throw e
            }
        }
        return BookCoverData(
            byteStream = s3Object.objectContent,
            contentLength = s3Object.objectMetadata.contentLength,
            contentType = s3Object.objectMetadata.contentType,
        )
    }

    fun saveCover(
        id: UUID,
        cover: MultipartFile,
    ) {
        s3.putObject(
            s3Properties.bucketName,
            id.toString(),
            cover.inputStream,
            ObjectMetadata().also {
                it.contentLength = cover.size
                it.contentType = cover.contentType
            }
        )
    }

}

data class BookCoverData(
    val byteStream: InputStream,
    val contentLength: Long,
    val contentType: String,
)
