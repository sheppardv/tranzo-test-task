FROM hseeberger/scala-sbt:11.0.2-oraclelinux7_1.3.13_2.11.12

WORKDIR /app

COPY target/scala-2.13/tranzo-test-task-assembly-1.0-SNAPSHOT.jar .

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "tranzo-test-task-assembly-1.0-SNAPSHOT.jar"]


