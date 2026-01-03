# Jenkins with Java 17, Maven 3.9.9, Allure 2.29.0
FROM jenkins/jenkins:2.479.2-lts-jdk17

# Skip setup wizard
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install plugins (must be readable by jenkins user)
COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Switch to root for tool installation
USER root

# Install Maven from Maven Central repository mirror
ARG MAVEN_VERSION=3.9.9
RUN curl -fsSL --retry 3 --retry-delay 5 https://repo1.maven.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar xzf - -C /opt \
    && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
    && ln -s /opt/maven/bin/mvn /usr/local/bin/mvn

# Install Allure CLI
ARG ALLURE_VERSION=2.29.0
RUN curl -fsSL https://github.com/allure-framework/allure2/releases/download/${ALLURE_VERSION}/allure-${ALLURE_VERSION}.tgz \
    | tar xzf - -C /opt \
    && ln -s /opt/allure-${ALLURE_VERSION} /opt/allure \
    && ln -s /opt/allure/bin/allure /usr/local/bin/allure

# Install utilities (jq for JSON parsing, curl for health checks)
RUN apt-get update \
    && apt-get install -y --no-install-recommends jq \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV PATH="/opt/maven/bin:/opt/allure/bin:${PATH}"
ENV MAVEN_HOME=/opt/maven
ENV ALLURE_HOME=/opt/allure

# Copy init scripts and set permissions
COPY --chown=jenkins:jenkins init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
RUN chmod 644 /usr/share/jenkins/ref/init.groovy.d/*.groovy

# Copy JCasC config
COPY --chown=jenkins:jenkins jenkins.yaml /usr/share/jenkins/ref/jenkins.yaml
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/jenkins.yaml

# Switch back to jenkins user
USER jenkins

# Expose ports
EXPOSE 8080 50000
