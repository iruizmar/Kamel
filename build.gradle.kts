import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.org.jetbrains.compose) apply false
    alias(libs.plugins.com.vanniktech.maven.publish) apply false
}

val file = project.file("local.properties")

if (file.exists()) {
    Properties().apply {
        val inputStream = file.inputStream()
        load(inputStream)
        ext {
            set("signing.keyId", getProperty("signing.keyId"))
            set("signing.password", getProperty("signing.password"))
            set("ossrh.username", getProperty("ossrh.username"))
            set("ossrh.password", getProperty("ossrh.password"))
            set("signing.secretKeyRingFile", getProperty("signing.secretKeyRingFile"))
            set("stagingProfileId", getProperty("stagingProfileId"))
        }
        inputStream.close()
    }
} else {
    ext {
        set("signing.keyId", System.getenv("SIGNING_KEY_ID"))
        set("signing.password", System.getenv("SIGNING_PASSWORD"))
        set("ossrh.username", System.getenv("OSSRH_USERNAME"))
        set("ossrh.password", System.getenv("OSSRH_PASSWORD"))
        set("signing.secretKeyRingFile", System.getenv("SIGNING_SECRET_KEY_RING_FILE"))
        set("stagingProfileId", System.getenv("STAGING_PROFILE_ID"))
    }
}

allprojects {

    group = Kamel.Group
    version = Kamel.Version

    val emptyJavadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

    afterEvaluate {
        extensions.findByType<PublishingExtension>()?.apply {
            publications.withType<MavenPublication>().configureEach {

                artifact(emptyJavadocJar.get())

                pom {

                    name.set("Kamel")
                    description.set("Kotlin Asynchronous Media Loading Library that supports Compose")
                    url.set("https://github.com/Kamel-Media/Kamel")

                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("alialbaali")
                            name.set("Ali Albaali")
                        }
                        developer {
                            id.set("luca992")
                            name.set("Luca Spinazzola")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/Kamel-Media/Kamel.git")
                        developerConnection.set("scm:git:ssh://github.com/alialbaali/Kamel.git")
                        url.set("https://github.com/Kamel-Media/Kamel/tree/main")
                    }

                }

            }

            repositories {
                maven {

                    name = "MavenCentral"

                    val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

                    url = if (version.toString().endsWith("SNAPSHOT")) uri(snapshotsRepoUrl) else uri(releasesRepoUrl)

                    credentials {
                        username = rootProject.ext["ossrh.username"] as String? ?: ""
                        password = rootProject.ext["ossrh.password"] as String? ?: ""
                    }

                }
            }

            extensions.findByType<SigningExtension>()?.sign(publications)
        }
    }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_1_8
    }
}