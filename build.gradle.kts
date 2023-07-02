plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "de.fxlae"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.uuid:java-uuid-generator:4.2.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
}

tasks.compileJava {
    options.release.set(8)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                "Implementation-Version" to project.version))
    }
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "typeid-java"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("typeid-java")
                description.set("A TypeID implementation for Java")
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

