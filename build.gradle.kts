import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.file.DuplicatesStrategy.INCLUDE
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.0" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    id("org.asciidoctor.jvm.convert") version "3.3.2" apply false

    kotlin("jvm") version "1.8.21" apply false
    kotlin("plugin.spring") version "1.8.21" apply false
    kotlin("plugin.jpa") version "1.8.21" apply false
    kotlin("plugin.noarg") version "1.8.21" apply false
}

allprojects {
    repositories { mavenCentral(); mavenLocal() }

    if (project.childProjects.isNotEmpty()) return@allprojects

    apply {
        plugin("io.spring.dependency-management")
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.jetbrains.kotlin:kotlin-bom:1.8.21")
            mavenBom("org.testcontainers:testcontainers-bom:1.18.3")
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }

        dependencies {
            dependency("com.ninja-squad:springmockk:4.0.2")
            dependency("io.mockk:mockk-jvm:1.13.5")
            dependency("org.testcontainers:junit-jupiter:1.18.1")
            dependency("org.testcontainers:localstack:1.18.1")
            dependency("com.amazonaws:aws-java-sdk-s3:1.12.272")
            dependency("com.amazonaws:aws-java-sdk-sts:1.12.272")
            dependency("io.kotest:kotest-assertions-core:5.6.2")
        }
    }

    tasks {
        withType<Copy> { duplicatesStrategy = INCLUDE }
        withType<Jar> { duplicatesStrategy = INCLUDE }
        withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "17"
                incremental = false
            }
        }
        withType<Test> {
            group = "verification"
            useJUnitPlatform()
            testLogging { events(FAILED, SKIPPED) }
        }
    }
}
