#! /bin/bash

MDT_WORKFLOW_HOME=$MDT_HOME/mdt-workflow-argo
MDT_BUILD_VERSION=1.2.1

docker image rmi kwlee0220/mdt-workflow-argo:$MDT_BUILD_VERSION

cp $MDT_WORKFLOW_HOME/mdt-workflow-argo-all.jar mdt-workflow-argo-all.jar

docker build -t kwlee0220/mdt-workflow-argo:$MDT_BUILD_VERSION .
# docker push kwlee0220/mdt-workflow-argo:$MDT_BUILD_VERSION

rm mdt-workflow-argo-all.jar
