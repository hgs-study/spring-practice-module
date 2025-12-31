#!/bin/bash

export DATAFLOW_VERSION=2.11.5
export SKIPPER_VERSION=2.11.5

# broker: kafka 또는 rabbitmq
BROKER=kafka

# database: postgres, mariadb, 또는 mysql (기본은 H2)
DATABASE=postgres

#docker-compose -f ../docker-compose.yml -f ../docker-compose-${BROKER}.yml up
docker-compose -f ../docker-compose.yml -f ../docker-compose-${BROKER}.yml -f ../docker-compose-${DATABASE}.yml up