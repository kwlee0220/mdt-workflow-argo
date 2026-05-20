FROM eclipse-temurin:21-jre-jammy

# UID/GID 설정 (빌드 시 --build-arg UID=1000 형태로 오버라이드 가능)
ARG UID=1000
ARG GID=1000

ENV MDT_HOME=/mdt

# mdt 사용자 생성 + 작업 디렉토리 준비 + 권한 부여 (단일 layer)
RUN groupadd -g ${GID} mdt \
 && useradd -u ${UID} -g mdt -m -s /bin/bash mdt \
 && mkdir -p /mdt/mdt-workflow/config \
             /mdt/airflow/dags \
             /mdt/mdt-workflow/logs \
 && chown -R mdt:mdt /mdt

COPY --chown=mdt:mdt ./mdt-workflow-argo-all.jar /mdt/mdt-workflow/

WORKDIR /mdt/mdt-workflow
USER mdt

CMD ["java", "-XX:MaxRAMPercentage=75.0", \
     "-jar", "/mdt/mdt-workflow/mdt-workflow-argo-all.jar"]
