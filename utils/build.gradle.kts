plugins {
    `java-library`
    signing
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "eu.pakithecat"

version = providers.gradleProperty("version")
    .orElse("0.1.0-SNAPSHOT")
    .get()

object Meta {
    const val description = "OSS GitHub Java Library Template Repository"

    const val license = "Apache-2.0"
    const val licenseUrl = "https://opensource.org/licenses/Apache-2.0"

    const val githubRepo = "concosminx/paki-utils"
    const val githubUrl = "https://github.com/$githubRepo"
    const val issuesUrl = "$githubUrl/issues"

    const val scmConnection = "scm:git:git://github.com/$githubRepo.git"
    const val scmDeveloperConnection = "scm:git:ssh://git@github.com/$githubRepo.git"

    const val developerId = "concosminx"
    const val developerName = "Cosmin C."
    const val developerOrganization = "ACME Corporation"
    const val developerOrganizationUrl = "https://pakithecat.eu"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val intTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val intTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

dependencies {
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.launcher)

    intTestImplementation(libs.junit)
    intTestRuntimeOnly(libs.junit.launcher)
    intTestImplementation(libs.bundles.testcontainers.junit)
    intTestImplementation(libs.assertj)
}

val intTest = tasks.register<Test>("intTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath

    shouldRunAfter(tasks.test)

    useJUnitPlatform()

    testLogging {
        events("passed")
    }
}

tasks.check {
    dependsOn(intTest)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

mavenPublishing {
    // Maven Central Publisher Portal.
    publishToMavenCentral(automaticRelease = true)

    // Sign all Maven publications.
    signAllPublications()

    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString(),
    )

    pom {
        name.set(project.name)
        description.set(Meta.description)
        url.set(Meta.githubUrl)

        licenses {
            license {
                name.set(Meta.license)
                url.set(Meta.licenseUrl)
            }
        }

        developers {
            developer {
                id.set(Meta.developerId)
                name.set(Meta.developerName)
                organization.set(Meta.developerOrganization)
                organizationUrl.set(Meta.developerOrganizationUrl)
            }
        }

        scm {
            url.set(Meta.githubUrl)
            connection.set(Meta.scmConnection)
            developerConnection.set(Meta.scmDeveloperConnection)
        }

        issueManagement {
            system.set("GitHub Issues")
            url.set(Meta.issuesUrl)
        }
    }
}

signing {
    val signingKey = providers.environmentVariable("GPG_SIGNING_KEY")
    val signingPassphrase = providers.environmentVariable("GPG_SIGNING_PASSPHRASE")

    if (signingKey.isPresent && signingPassphrase.isPresent) {
        useInMemoryPgpKeys(
            signingKey.get(),
            signingPassphrase.get(),
        )
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }
}

// Gradle dependency locking.
// Required/used for Trivy scanning.
dependencyLocking {
    lockAllConfigurations()
}

// Always run subproject dependency task with parent.
rootProject.tasks.dependencies {
    dependsOn(tasks.dependencies)
}
