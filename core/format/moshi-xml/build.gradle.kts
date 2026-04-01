description = "http4k XML support using Moshi and org.json as an underlying engine"

plugins {
    id("org.http4k.community")
}

dependencies {
    api(project(":http4k-realtime-core"))
    api(project(":http4k-format-moshi"))
    api(libs.json)

    testImplementation(project(":http4k-core"))
    testImplementation(libs.values4k)
    testImplementation(testFixtures(project(":http4k-core")))
    testImplementation(testFixtures(project(":http4k-format-core")))
}
