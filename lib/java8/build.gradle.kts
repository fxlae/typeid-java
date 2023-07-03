plugins {
    id("typeid.java-conventions")
    id("typeid.library-conventions")
}

tasks.compileJava {
    options.release.set(8)
}

dependencies {
    "provided"(project(":lib:shared"))
    implementation(libs.java.uuid.generator)
    testImplementation(project(path = ":lib:shared", configuration = "testArtifacts"))
}
