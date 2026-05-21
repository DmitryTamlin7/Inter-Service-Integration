plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    jacoco
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.0")
    implementation("org.springframework.boot:spring-boot-starter-amqp:3.2.0")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("io.minio:minio:8.5.7")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation ("org.springframework.boot:spring-boot-testcontainers")
    testImplementation ("org.testcontainers:junit-jupiter")
    testImplementation ("org.testcontainers:postgresql")
    testImplementation ("org.testcontainers:testcontainers")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}



tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }

    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "**/config/**/*.class",
                "**/config/*",
                "**/dto/**/*.class",
                "**/dto/*",
                "**/domain/**/*.class",
                "**/domain/*",
                "**/*Application*.class"
            )
        }
    }))
}
