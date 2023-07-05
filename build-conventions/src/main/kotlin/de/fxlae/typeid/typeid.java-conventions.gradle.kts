plugins {
    java
    jacoco
}

repositories {
    mavenCentral()
}

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    versionCatalog.findLibrary("junit.bom").ifPresent {
        testImplementation(platform(it))
    }
    testImplementation("org.junit.jupiter:junit-jupiter")
    versionCatalog.findLibrary("jackson.dataformat.yaml").ifPresent {
        testImplementation(it)
    }
    versionCatalog.findLibrary("assertj.core").ifPresent {
        testImplementation(it)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
