plugins {
    id("typeid.java-conventions")
    id("typeid.library-conventions")
    alias(libs.plugins.jmh)
}

tasks.compileJava {
    options.release = 17
}

dependencies {
    implementation(libs.java.uuid.generator)
}

jmh {
    warmupIterations.set(3)
    iterations.set(2)
    threads.set(1)
    fork.set(1)
    //includes.set(listOf("TypeIdBench.parseWithError*"))
}
