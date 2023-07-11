FROM java:17
VOLUME /tmp
COPY target/syncDns-0.0.1-SNAPSHOT.jar app.jar
RUN bash -c "touch /demo.jar"
EXPOSE 8089
# ENTRYPOINT ["java","-jar","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=10599","app.jar"]
ENTRYPOINT ["java","-jar","app.jar"]
