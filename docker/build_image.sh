#! /bin/bash

docker image rmi kwlee0220/mdt-workflow-argo

cp ../build/libs/mdt-workflow-argo-1.0.0-all.jar mdt-workflow-argo.jar

docker build -t kwlee0220/mdt-workflow-argo:latest .

rm mdt-workflow-argo.jar
