plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
}

allprojects {
    group = "com.InterServiceIntegration"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}


