#!/bin/sh


# Create secret to s3-to-kafka integration
oc delete secret env-cfg-demo
oc create secret generic --from-env-file=./config/env.cfg env-cfg-demo

# Deploy s3-to-kafka integration 
oc apply -f s3-to-kafka/s3-to-kafka.yaml