package example.aws.s3.domain

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Contains the name of our bucket as well as predefined book ids we use as example data.
 */
@ConfigurationProperties(prefix = "example.s3")
data class S3Properties(
    val bucketName: String,
    val books: Books,
) {
    data class Books(
        val cleanCode: String,
        val lordOfTheRings: String,
    )
}