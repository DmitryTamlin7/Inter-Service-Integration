plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("jacoco")
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
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")

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
        }

        doLast {
            // layout.buildDirectory.file(...) указывает строго на build-папку текущего субпроекта
            val reportFile = layout.buildDirectory.file("reports/jacoco/test/html/index.html").get().asFile

            if (reportFile.exists()) {
                val content = reportFile.readText()

                // Ищем финальный процент в tfoot HTML-отчета
                val regex = """<tfoot>.*?(\d{1,3})%""".toRegex(RegexOption.DOT_MATCHES_ALL)
                val match = regex.find(content)

                val percentage = match?.groups?.get(1)?.value ?: "0"

                println("\n==============================================")
                println("📊 СЕРВИС: [${project.name.uppercase()}]")
                println("📊 ПОКРЫТИЕ КОДА ТЕСТАМИ (JaCoCo): $percentage%")
                println("==============================================\n")
            } else {
                println("\n⚠️ [${project.name.uppercase()}]: Отчет не найден по пути: ${reportFile.absolutePath}")
            }
        }
    }
}