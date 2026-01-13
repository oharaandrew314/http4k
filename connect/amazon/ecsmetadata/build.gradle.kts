plugins {
    `java-test-fixtures`
    id("com.google.devtools.ksp")
}

dependencies {
    api("org.http4k:http4k-connect-amazon-core:_")
    ksp("se.ansman.kotshi:compiler:_")

    testFixturesApi("org.http4k:http4k-testing-chaos")
}
