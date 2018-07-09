plugins {
  `java-library`
  kotlin("jvm") version "1.2.51"
}
tasks.getByName<Wrapper>("wrapper") {
  gradleVersion = "4.8.1"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_7
  targetCompatibility = JavaVersion.VERSION_1_7
}

repositories {
  jcenter()
}
dependencies {
  implementation(kotlin("stdlib"))
  api("com.google.guava:guava:20.0")

  testImplementation("junit:junit:4.12")
}
