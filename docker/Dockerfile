FROM eclipse-temurin:21.0.7_6-jdk


ENV MDT_HOME=/mdt
RUN mkdir -p /mdt/mdt-workflow-argo/config

COPY ./mdt-workflow-argo-all.jar /mdt/mdt-workflow-argo/

WORKDIR /mdt/mdt-workflow-argo
CMD ["java", "-jar", "mdt-workflow-argo-all.jar"]
