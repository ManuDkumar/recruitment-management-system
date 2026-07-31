# Artifact-based image: build the jar first with `mvnw package` (or in CI),
# then containerize the artifact. Fast and reliable compared to building
# inside the container, where Maven must re-download all dependencies.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/recruitment-app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
