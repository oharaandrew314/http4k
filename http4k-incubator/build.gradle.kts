description = "http4k incubator module"

dependencies {
    api(project(":http4k-core"))
    api(project(":http4k-format-moshi"))
    api(Square.moshi.adapters)
    implementation(project(mapOf("path" to ":http4k-testing-webdriver")))
    compileOnly(Testing.junit.jupiter.api)

    // boost
    api(project(":http4k-contract"))
    api(project(":http4k-server-undertow"))
    api(project(":http4k-cloudnative"))
    api("dev.forkhandles:result4k:_")
    api("dev.forkhandles:values4k:_")
    api("dev.forkhandles:time4k:_")

    testImplementation(project(":http4k-client-apache"))
    testImplementation(testFixtures(project(":http4k-core")))
    testImplementation(project(path = ":http4k-testing-approval"))
    testImplementation(testFixtures(project(":http4k-contract")))
}
