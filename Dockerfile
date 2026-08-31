FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
COPY services services
ARG MODULE
RUN mvn -q -pl services/${MODULE} -am -DskipTests package
FROM eclipse-temurin:25-jre
ARG MODULE
COPY --from=build /workspace/services/${MODULE}/target/${MODULE}-1.0.0-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
