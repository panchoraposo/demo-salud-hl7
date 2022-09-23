#!/bin/sh

# aws cli - create s3
aws s3api delete-bucket --bucket salud-hl7-demo --region us-east-1
aws s3api create-bucket --bucket salud-hl7-demo --region us-east-1

# Deploy mongodb
oc delete deploy mongodb-hl7-demo
oc delete svc mongodb-hl7-demo
oc new-app mongo MONGO_INITDB_ROOT_USERNAME=demo MONGO_INITDB_ROOT_PASSWORD=demo --name=mongodb-hl7-demo -l app.openshift.io/runtime=mongodb

# Create project
oc new-project integrations-demo
oc project integrations-demo

# Create secret to demo
oc delete secret env-cfg-demo
oc create secret generic --from-env-file=./config/env.cfg env-cfg-demo

# Deploy s3-to-kafka integration 
oc delete integration s3-to-kafka
oc apply -f ./s3-to-kafka/s3-to-kafka.yaml

# Deploy hl7-to-json
# cd ./hl7-to-json/
# quarkus build -Dquarkus.kubernetes.deploy=true
# cd ..

# Deploy kafka-to-nosql 
oc delete integration kafka-to-nosql
oc apply -f kafka-to-nosql/kafka-to-nosql.yaml