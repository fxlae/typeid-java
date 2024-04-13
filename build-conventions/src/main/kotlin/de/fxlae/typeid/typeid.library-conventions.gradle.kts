plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "de.fxlae"
version = "0.3.0-SNAPSHOT"

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to mavenArtifactId,
                "Implementation-Version" to project.version
            )
        )
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

tasks.named("jar").configure {
    enabled = false
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

val mavenArtifactId: String by project
val mavenArtifactDescription: String by project

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = mavenArtifactId
            from(components["java"])
            pom {
                name.set(mavenArtifactId)
                description.set(mavenArtifactDescription)
                url.set("https://github.com/fxlae/typeid-java")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("fxlae")
                        name.set("Felix Lägeler")
                        email.set("felixlaegeler@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:fxlae/typeid-java.git")
                    developerConnection.set("scm:git:git@github.com:fxlae/typeid-java.git")
                    url.set("https://github.com/fxlae/typeid-java/")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}
