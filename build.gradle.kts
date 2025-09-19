import org.jreleaser.model.Active
import org.jreleaser.model.Signing

plugins {
    kotlin("jvm") version "1.9.22"
    `java-library`
    `maven-publish`
    id("org.jreleaser") version "1.13.1"
}

group = "com.ebmlibs"
version = "1.1.0"
base {
    archivesName = rootProject.name
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }

                pom {
                    name.set("GenericPieceTable")
                    description.set("GenericPieceTable is a library that provides a piece table data structure for efficient text editing.")
                    url.set("https://github.com/timaviciix/GenericPieceTable")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("TIMAVICIIX")
                            name.set("TIMAVICIIX")
                            email.set("timaviciix@outlook.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/timaviciix/GenericPieceTable.git")
                        developerConnection.set("scm:git:ssh://github.com/timaviciix/GenericPieceTable.git")
                        url.set("https://github.com/timaviciix/GenericPieceTable")
                    }
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

//jreleaser {
//    signing {
//        active.set(Active.ALWAYS)
//        armored.set(true)
//        mode.set(org.jreleaser.model.Signing.Mode.FILE)
//        publicKey.set("D:\\Develop_Space\\Key\\public.pgp")
//        secretKey.set("D:\\Develop_Space\\Key\\private.pgp")
//        passphrase.set(System.getenv("JRELEASER_GPG_PASSPHRASE"))
//    }
//
//    deploy {
//        maven {
//            mavenCentral {
//                create("sonatype") {
//                    username.set(System.getenv("MAVENCENTRAL_USERNAME"))
//                    password.set(System.getenv("MAVENCENTRAL_PASSWORD"))
//                    active.set(Active.ALWAYS)
//                    url.set("https://central.sonatype.com/api/v1/publisher")
//                    stagingRepository(System.getProperty("user.home") + "/.m2/repository")
//                }
//            }
//        }
//    }
//}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.testng:testng:7.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}