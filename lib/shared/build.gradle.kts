plugins {
    id("typeid.java-conventions")
}

tasks.compileJava {
    options.release.set(8)
}

configurations {
    create("testArtifacts")
}

tasks.register<Jar>("testJar") {
    from(sourceSets["test"].output)
    archiveClassifier.set("tests")
}

artifacts {
    add("testArtifacts", tasks["testJar"])
}

dependencies {
    implementation("com.fasterxml.uuid:java-uuid-generator:4.2.0")
}
