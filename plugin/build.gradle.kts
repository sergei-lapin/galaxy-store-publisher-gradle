import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin)
  alias(libs.plugins.dokka)
  alias(libs.plugins.pluginPublish)
  id("java-gradle-plugin")
}

group = "com.sergei-lapin.galaxy-store-publisher"

version = "1.0.0-alpha03"

val jvmTarget = JavaVersion.VERSION_11.toString()

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = jvmTarget }

tasks.withType<JavaCompile> {
  sourceCompatibility = jvmTarget
  targetCompatibility = jvmTarget
}

java {
  withJavadocJar()
  withSourcesJar()
}

tasks.named<Jar>("javadocJar") { from(tasks.named("dokkaJavadoc")) }

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleKotlinDsl())
  compileOnly(libs.agp)

  implementation(libs.jwt)
  implementation(libs.gson)
  implementation(libs.okhttp)

  dokkaHtmlPlugin(libs.dokkaKotlinAsJavaPlugin)

  testImplementation(gradleTestKit())
}

gradlePlugin {
  plugins.create("galaxy-store-publisher") {
    id = group.toString()
    displayName = "Galaxy Store Publisher"
    description = "The Gradle Plugin for automated distribution to Samsung Galaxy Store"
    implementationClass = "com.slapin.gsp.GalaxyStorePublisherPlugin"
  }
}

pluginBundle {
  website = "https://github.com/sergei-lapin/galaxy-store-publisher-gradle"
  vcsUrl = "https://github.com/sergei-lapin/galaxy-store-publisher-gradle.git"
  tags = listOf("distribution", "galaxy", "store", "publishing")
  description = "Plugin that automates distribution to Samsung Galaxy Store"
}
