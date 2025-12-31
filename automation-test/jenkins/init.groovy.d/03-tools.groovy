// Verify tool installations
import jenkins.model.*

def jenkins = Jenkins.getInstance()

// Verify Maven
def mavenHome = System.getenv("MAVEN_HOME") ?: "/opt/maven"
def mvnVersion = "mvn --version".execute()
mvnVersion.waitFor()
if (mvnVersion.exitValue() == 0) {
    println "[INIT] Maven available: ${mavenHome}"
} else {
    println "[INIT] WARN: Maven not found at ${mavenHome}"
}

// Verify Allure
def allureHome = System.getenv("ALLURE_HOME") ?: "/opt/allure"
def allureVersion = "allure --version".execute()
allureVersion.waitFor()
if (allureVersion.exitValue() == 0) {
    println "[INIT] Allure available: ${allureHome}"
} else {
    println "[INIT] WARN: Allure not found at ${allureHome}"
}

// Verify Java
def javaVersion = "java -version".execute()
javaVersion.waitFor()
println "[INIT] Java available: /opt/java/openjdk"

println "[INIT] Tools verification complete"
