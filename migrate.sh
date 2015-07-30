#!/usr/bin/env bash

docker stop test-postgres
docker rm test-postgres

docker run -d -p 5432:5432 --name test-postgres postgres:9.4

DB_IP=`boot2docker ip`

./gradlew clean shadowJar

JDBC_URL="jdbc:postgresql://${DB_IP}/postgres"

java -jar build/libs/liquibase-docker-postgres-1.1-SNAPSHOT-all.jar --driver=org.postgresql.Driver \
     --classpath=build/libs/liquibase-docker-postgres-1.1-SNAPSHOT-all.jar \
     --changeLogFile=db/changelog/db.changelog-master.xml \
     --url=${JDBC_URL} \
     --username=postgres \
     --password=postgres \
     migrate

./gradlew -Djdbc.url=${JDBC_URL} integrationTest