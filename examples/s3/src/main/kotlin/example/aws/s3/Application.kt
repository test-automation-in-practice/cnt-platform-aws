package example.aws.s3

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

/**
 * Start the application from a builder (instead of the usual [runApplication]) so we can supply our [S3Initializer].
 */
fun main(args: Array<String>) {
    SpringApplicationBuilder(Application::class.java)
        .initializers(S3Initializer())
        .run(*args)
}
