import java.util.*
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("idea")
  id("com.android.library")
  kotlin("android") // version(Versions.plugins.kotlin)
  kotlin("android.extensions") // version(Versions.plugins.kotlin)
  id("kotlin-sam-with-receiver")
  id("kotlin-noarg")
  id("kotlin-kapt")
  id("signing")
  id("com.jfrog.bintray")
  id("maven-publish")
}

val localPropMap = mutableMapOf<Project, Properties>()


val POM_ARTIFACT_ID = getProjectProperty("POM_ARTIFACT_ID")
val POM_DESCRIPTION = getProjectProperty("POM_DESCRIPTION")
val VERSION_NAME = getProjectProperty("VERSION_NAME")
val publishedGroupId = getProjectProperty("GROUP")

group =publishedGroupId as String
version = VERSION_NAME as String


repositories {
  mavenLocal()
  gradlePluginPortal()
  google()
  jcenter()
  mavenCentral()
  maven(url = "https://repo1.maven.org/maven2")
  maven(url = "https://kotlin.bintray.com/kotlinx")
  maven(url = "https://dl.bintray.com/densebrain/oss")
  maven(url = "https://jitpack.io")
  maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.31")
  testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.31")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.3.31")
}

tasks.withType(KotlinCompile::class).all({
  kotlinOptions {
    jvmTarget = "1.8"
    apiVersion = "1.3"
    languageVersion = "1.3"
    //allWarningsAsErrors = true
  }
})

configurations.all {
  resolutionStrategy {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jre7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jre8")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")

    force(
      "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.31"
    )
  }
}

android {
  setCompileSdkVersion(28)
  buildToolsVersion = "28.0.3"


  useLibrary("android.test.runner")
  useLibrary("android.test.base")
  useLibrary("android.test.mock")

  defaultConfig {
    setMinSdkVersion(15)
    setTargetSdkVersion(28)
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    multiDexEnabled = true

  }

  testOptions {
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
  }

  sourceSets.forEach { sourceSet ->
    sourceSet.java.srcDirs("${projectDir}/src/main/kotlin")
  }
}


/**
 * Configure publish
 */

afterEvaluate {

  val sourcesJar by tasks.registering(Jar::class) {
    from(android.sourceSets["main"].java.srcDirs)
    archiveClassifier.set("sources")
  }

  artifacts.add("archives", sourcesJar)

  project.components.forEach { component ->
    logger.quiet("Component: ${component}")
  }

  tasks.withType(Javadoc::class).all {
    enabled = false
  }


  lateinit var allPublications: PublicationContainer
  configure<PublishingExtension> {
    repositories {
      mavenLocal()
    }

    allPublications = publications

    publications.create(project.name, MavenPublication::class.java) {
      groupId = publishedGroupId
      artifactId = POM_ARTIFACT_ID
      version = VERSION_NAME

      artifact(tasks.getByName("bundleReleaseAar"))
      artifact(sourcesJar.get())

//      pom.withXml {
//        asNode().appendNode("dependencies").apply {
//          fun Dependency.write(scope: String) = appendNode("dependency").apply {
//            //appendNode("groupId", GROUP)
//            //appendNode("artifactId", POM_ARTIFACT_ID)
//            appendNode("version", if (version == null || version == "unspecified")
//              VERSION_NAME
//            else
//              version
//            )
//            appendNode("scope", scope)
//          }
//
//          for (dependency in configurations["api"].dependencies) {
//            dependency.write("compile")
//          }
//          for (dependency in (configurations["compile"].dependencies + configurations["implementation"].dependencies)) {
//            dependency.write("runtime")
//          }
//        }
//      }
    }


  }

  tasks {
    withType<AbstractPublishToMaven> {
      dependsOn(
        getByName("assembleRelease")
      )
    }


    rootProject.tasks
      .getByName("publish")
      .dependsOn(withType(BintrayUploadTask::class))
  }
  configure<BintrayExtension> {
    val bintrayRepo = getProperty(
      arrayOf("bintrayRepo", "BINTRAY_REPO"),
      arrayOf("bintrayRepo", "BINTRAY_REPO"),
      "oss"
    )

    val bintrayUsername = getProperty(
      arrayOf("bintrayUsername", "BINTRAY_USERNAME"),
      arrayOf("bintrayUsername", "BINTRAY_USERNAME"),
      "jonglanz"
    )

    val bintrayUserOrg = getProperty(
      arrayOf("BINTRAY_USER_ORG"),
      arrayOf("bintrayUserOrg", "BINTRAY_USER_ORG"),
      "densebrain"
    )

    val bintrayApiKey = getProperty(
      arrayOf("bintrayApiKey", "BINTRAY_API_KEY"),
      arrayOf("bintrayApiKey", "BINTRAY_API_KEY"),
      null
    )

    val POM_LICENSE_NAME = getProperty(arrayOf("POM_LICENSE_NAME"),
      arrayOf("POM_LICENSE_NAME"),
      "MIT"
    )
    val bintrayName = "${publishedGroupId}:${POM_ARTIFACT_ID}"
    val bintrayDescription = POM_DESCRIPTION
    val projectUrl = getProjectProperty("POM_URL")
    val issuesUrl = "https://github.com/facebook/stato/issues"
    val scmUrl = getProjectProperty("POM_SCM_URL")
    val scmConnection = getProjectProperty("POM_SCM_CONNECTION")
    val scmDeveloperConnection = getProjectProperty("POM_SCM_DEV_CONNECTION")

    val developerId = getProjectProperty("POM_DEVELOPER_ID")
    val developerName = getProjectProperty("POM_DEVELOPER_NAME")

    if (bintrayUsername != null && bintrayApiKey != null) {
      user = bintrayUsername
      key = bintrayApiKey

      //setConfigurations("archives")
      setPublications(*allPublications.map { it.name }.toTypedArray())

      with(pkg) {
        repo = bintrayRepo
        userOrg = bintrayUserOrg
        name = bintrayName
        desc = bintrayDescription
        websiteUrl = projectUrl
        issueTrackerUrl = issuesUrl
        vcsUrl = scmUrl
        setLicenses(POM_LICENSE_NAME)
        override = true
        publish = true
        publicDownloadNumbers = true

        with(version) {
          name = VERSION_NAME
          desc = bintrayDescription

        }
      }

    }
  }


}



val Project.localProps: Properties
  get() = localPropMap.getOrPut(project) {

    val props = Properties()
    try {
      val file = File(projectDir, "local.properties")
      if (file.exists()) {
        file.inputStream().use { stream ->
          props.load(stream)
        }
      }
    } catch (ex:Throwable) {
      logger.quiet("Unable to load props", ex)
    }
    props
  }

fun Project.getProjectProperty(vararg names: String): String? {
  for (name in names) {
    if (localProps.containsKey(name))
      return localProps.getProperty(name)

    if (rootProject.localProps.containsKey(name))
      return rootProject.localProps.getProperty(name)

    if (project.hasProperty(name))
      return project.properties[name] as String?

    if (rootProject.hasProperty(name))
      return rootProject.properties[name] as String?
  }

  return null
}

fun getSystemProperty(vararg names: String): String? {
  for (name in names) {
    val value = System.getenv(name)
    if (value != null)
      return value
  }

  return null
}

fun Project.getProperty(systemProps: Array<String>, projectProps: Array<String>, defaultValue: String?): String? {
  return getSystemProperty(*systemProps) ?: getProjectProperty(*projectProps) ?: defaultValue

}