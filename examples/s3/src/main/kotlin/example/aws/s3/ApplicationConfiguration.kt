package example.aws.s3

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import example.aws.s3.domain.S3Properties
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.context.support.GenericApplicationContext
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.util.function.Supplier

@Configuration
class ApplicationConfiguration(

    /**
     * There is no real [AmazonS3] bean available to us, we artificially create and inject our own using the
     * [S3Initializer] below. IntelliJ does not know that, so we have to suppress its inspections about missing beans.
     */
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val s3: AmazonS3,
    private val s3properties: S3Properties,
) {

    /**
     * Our S3 is not permanent, but is instead re-created every time the app is running. Therefore, we also have to
     * create a bucket every time the container starts.
     *
     * We also use this opportunity to add 2 book covers into the bucket to serve as pre-made examples.
     */
    @EventListener
    fun createBucketAddExamples(event: ApplicationStartedEvent) {
        s3.createBucket(s3properties.bucketName)

        val cleanCodeCover = File(this::class.java.getResource("/bookcovers/clean_code.jpg")!!.path)
        val lotrCover = File(this::class.java.getResource("/bookcovers/fellowship_of_the_ring.jpg")!!.path)

        s3.putObject(s3properties.bucketName, s3properties.books.cleanCode, cleanCodeCover)
        s3.putObject(s3properties.bucketName, s3properties.books.lordOfTheRings, lotrCover)
    }
}

/**
 * This [ApplicationContextInitializer] is the means by which we create an S3 instance, both for our tests, and also as
 * a substitute for not having a real AWS environment in this simple example.
 */
class S3Initializer : ApplicationContextInitializer<GenericApplicationContext> {

    override fun initialize(applicationContext: GenericApplicationContext) {
        val container = S3Container().withServices(LocalStackContainer.Service.S3).apply { start() }
        createS3Bean(container, applicationContext)
    }

    /**
     * Normally you would declare a [Bean] method where you create and configure your [AmazonS3] client. Since we use
     * a Test Container we instead inject our client directly in Spring's application context.
     */
    private fun createS3Bean(
        container: LocalStackContainer,
        applicationContext: GenericApplicationContext
    ) {
        applicationContext.registerBean(
            AmazonS3::class.java.simpleName,
            AmazonS3::class.java,
            Supplier {
                AmazonS3ClientBuilder
                    .standard()
                    .withEndpointConfiguration(
                        AwsClientBuilder.EndpointConfiguration(
                            container.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
                            container.region
                        )
                    )
                    .withCredentials(
                        AWSStaticCredentialsProvider(BasicAWSCredentials(container.accessKey, container.secretKey))
                    )
                    .build()
            }
        )
    }

    private class S3Container : LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
}
