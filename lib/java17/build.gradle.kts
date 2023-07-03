plugins {
    id("typeid.java-conventions")
    id("typeid.library-conventions")
    alias(libs.plugins.jmh)
}

tasks.compileJava {
    options.release.set(17)
}

dependencies {
    "provided"(project(":lib:shared"))
    implementation(libs.java.uuid.generator)
    testImplementation(project(path = ":lib:shared", configuration = "testArtifacts"))
    jmh(project(":lib:shared"))
}

jmh {
    warmupIterations.set(3)
    iterations.set(2)
    threads.set(1)
    fork.set(1)
    //includes.set(listOf("TypeIdBench.parseWithError*"))
}
