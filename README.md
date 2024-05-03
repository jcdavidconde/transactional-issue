# Digital Asset Management

This service is the backend for the Digital Asset Management platform. It provides the endpoints needed to manage digital content templates
and access them for use in creating content.

## This project uses

* Java 17
* Kotlin 1.8.0
* Micronaut 4.2.0
* Gradle 7.4.2
* Ktlint 11.0.0

## Running locally

* Start the MySQL server locally
  * Under the root directory of this git repo, execute `docker compose up -d`
* Start the app `./gradlew run`
* Hit the endpoint http://localhost:5000

The `http` folder contains files that have requests for the endpoints (e.g. asset.http, folder.http, internal.http)
