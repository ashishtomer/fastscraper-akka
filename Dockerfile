# we will use openjdk 8 with alpine as it is a very small linux distro
FROM openjdk:11-jre-alpine3.9

EXPOSE 8082

# copy the packaged jar file into our docker image
COPY target/scala-2.13/${APP_NAME}-${APP_VERSION}.jar /fastscraper.jar

# set the startup command to execute the jar
CMD ["java", "-jar", "/fastscraper.jar"]