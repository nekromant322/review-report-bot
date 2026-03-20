FROM maven:3.6.3-jdk-11-slim AS build
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
RUN mvn -B -f pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jdk
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
    "-Dhttp.proxyHost=host.docker.internal", \
    "-Dhttp.proxyPort=2081", \
    "-Dhttps.proxyHost=host.docker.internal", \
    "-Dhttps.proxyPort=2081", \
    "-jar", "app.jar"]
