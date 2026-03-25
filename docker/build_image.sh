#! /bin/bash

# --tag(-t) 옵션 처리 및 MDT_VERSION 변수 설정
MDT_VERSION=""
HOST_USER=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --tag|-t)
            MDT_VERSION="$2"
            shift 2
            ;;
        --host-user|-H)
            HOST_USER=true
            shift 1
            ;;
        *)
            break
            ;;
    esac
done

# MDT_VERSION이 지정되지 않았으면 기본값 사용
if [ -z "$MDT_VERSION" ]; then
    MDT_VERSION="$MDT_BUILD_VERSION"
fi
REPOSITORY="mdt-workflow:$MDT_VERSION"

# 사용법 출력 (--help 옵션)
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "사용법: $0 [--tag <VERSION>] [--host-user|-H]"
    echo ""
    echo "옵션:"
    echo "  --tag, -t <VERSION> 버전 지정 (예: 1.3.0). 기본값은 \$MDT_BUILD_VERSION"
    echo "  --host-user, -H    빌드 시 호스트 사용자의 UID/GID를 사용"
    echo "  --help, -h              도움말 출력"
    echo ""
    echo "예제:"
    echo "  $0                        # 기본 버전으로 빌드"
    echo "  $0 --tag 1.3.0        # 1.3.0 버전으로 빌드"
    echo "  $0 -H                   # 호스트 UID/GID로 빌드"
    exit 0
fi

# 기존 이미지 삭제
docker image rmi -f $REPOSITORY

echo "==> Docker 이미지 빌드 시작: $REPOSITORY"
MDT_WORKFLOW_HOME=$MDT_HOME/mdt-workflow
cp $MDT_WORKFLOW_HOME/mdt-workflow-argo-all.jar mdt-workflow-argo-all.jar

# Docker build args 구성
DOCKER_BUILD_ARGS=()
if [ "$HOST_USER" = true ]; then
    HOST_UID="$(id -u)"
    HOST_GID="$(id -g)"
    DOCKER_BUILD_ARGS+=(--build-arg "UID=${HOST_UID}" --build-arg "GID=${HOST_GID}")
fi

# Docker 이미지 빌드
docker build -t $REPOSITORY "${DOCKER_BUILD_ARGS[@]}" .

# 성공 메시지
if [ $? -eq 0 ]; then
    echo "==> 빌드 완료: $REPOSITORY"
else
    echo "==> 빌드 실패!"
    exit 1
fi

# 클론한 디렉토리 정리
rm mdt-workflow-argo-all.jar