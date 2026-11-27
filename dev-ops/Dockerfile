FROM eclipse-temurin:21-jre
WORKDIR /app

COPY target/ai-project-api-0.0.1.jar /app/app.jar

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar", "--server.address=0.0.0.0"]
