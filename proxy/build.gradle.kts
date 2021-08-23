repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(project(":messaging-framework-common"))
    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")
}
