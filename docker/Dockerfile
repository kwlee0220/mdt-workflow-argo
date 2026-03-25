FROM eclipse-temurin:21.0.7_6-jdk

# UID/GID 설정 (빌드 시 --build-arg UID=1000 형태로 오버라이드 가능)
ARG UID=1000
ARG GID=1000

ENV MDT_HOME=/mdt

RUN mkdir -p /mdt/mdt-workflow/config
RUN mkdir -p /mdt/airflow/dags
RUN mkdir -p /mdt/mdt-workflow/logs

COPY ./mdt-workflow-argo-all.jar /mdt/mdt-workflow/

WORKDIR /mdt/mdt-workflow

# 사용자/그룹 이름 충돌을 피하기 위해, 숫자 UID/GID로 소유권을 부여하고 그대로 실행합니다.
RUN chown -R ${UID}:${GID} /mdt
USER ${UID}:${GID}

CMD ["java", "-jar", "mdt-workflow-argo-all.jar"]
