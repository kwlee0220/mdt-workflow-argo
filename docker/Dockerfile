FROM eclipse-temurin:21.0.7_6-jdk

RUN mkdir -p /mdt/mdt-workflow-argo
COPY ./mdt-workflow-argo.jar /

ENV MDT_HOME=/mdt

WORKDIR /mdt/mdt-workflow-argo
ENTRYPOINT ["java", "-jar", "/mdt-workflow-argo.jar"]
