# hmpps-prisoner-base-location-api

[![Ministry of Justice Repository Compliance Badge](https://github-community.service.justice.gov.uk/repository-standards/api/hmpps-prisoner-base-location-api/badge?style=flat)](https://github-community.service.justice.gov.uk/repository-standards/hmpps-prisoner-base-location-api)
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-prisoner-base-location-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://prisoner-base-location-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)
[![Pipeline [test -> build -> deploy]](https://github.com/ministryofjustice/hmpps-education-employment-api/actions/workflows/pipeline.yml/badge.svg?branch=main)](https://github.com/ministryofjustice/hmpps-prisoner-base-location-api/actions/workflows/pipeline.yml)

# About
The purpose of this repo is to create an internal API that can be called by the Integration API to expose prisoner base location information that has been translated into a less NOMIS model for external use

## Team
This integration service is developed and supported by `Education Skills & Work` team. They can be contacted via `#education-skills-work-employment-dev` on Slack.

# Instructions

## Running the application locally

The application comes with a `dev` spring profile that includes default settings for running locally. This is not
necessary when deploying to kubernetes as these values are included in the helm configuration templates -
e.g. `values-dev.yaml`.

There is also a `docker-compose.yml` that can be used to run a local instance of the template in docker.

This API application depends on several services to run.

| Dependency            | Description                                   | Default | Override Env Var                                        |
|---------------------  |-----------------------------------------------|---------|---------------------------------------------------------|
| hmpps-auth            | OAuth2 API server for authenticating requests |         | `HMPPS_AUTH_URL`                                        |
| Prisoner Search API   | API for retrieving prisoner profile           |         | `API_CLIENT_CLIENTS_PRISONEROFFENDERSEARCH_BASE_URL`  |


### Preparation
Obtain API client credentials
- populate those value from kubernetes secrets (`hmpps-prisoner-base-location-api-client-creds`)
  ```shell
  kubectl -n hmpps-prisoner-base-location-dev get secret hmpps-prisoner-base-location-api-client-creds -o json | jq '.data | map_values(@base64d)' 
  ```
- fill in the API client credentials in these files: `API_CLIENT_ID` and `API_CLIENT_SECRET`
    - `.env` for running outside docker
    - `.env.docker` for running in docker

---
### Running with docker compose
The easiest way to run the app is to use docker compose to create the service and all dependencies.
1. Prepare `.env.docker` (from `.env.docker.sample`)
    ```shell
    cp .env.docker.sample .env.docker
    ```
    - fill in the API client credentials in `.env.docker`
      see above to obtain these
    - in case of `$` in value, escape them (with `$$`)
2. Then run
   ```shell
   docker compose up
   ```
   will run the application (from latest image) and PostgreSQL within a local docker instance.
3. Check if application is up and running
    * See `http://localhost:8080/health` to check the app is running.
    * See `http://localhost:8080/swagger-ui/index.html` to explore the OpenAPI spec document.
    * See `http://localhost:8080/info` to check the app info

It connects HMPPS Auth and other upstream APIs in `dev` environment. Thus, a set of valid dev API clients are required to run the application.

---
### Running the application in IntelliJ
1. Prepare `.env` (from `.env.local.sample`)
    ```shell
    cp .env.local.sample .env
    ```
    - fill in the API client credentials in `.env`:
      see above to obtain these
2. Run `bootRun` with  `.env` file prepared above
    * either IntelliJ
        - run `bootRun` with `EnvFile` plugin
        - add `.env`
        - enable integrations
    * or Gradle wrapper
      ```shell
      export $(grep -v '^#' .env | xargs)
      ./gradlew bootRun
      ```

### Environment variables
Defining env var for *local* run

| Env. var.           | description                                         |
|---------------------|-----------------------------------------------------|
| `API_CLIENT_ID`     | API client ID for accessing Prisoner Search API     |
| `API_CLIENT_SECRET` | API client secret for accessing Prisoner Search API |
_*_ These values can be obtained from k8s secrets in `dev` env.


## Run docker image on local

### Build a local docker image
1. Build the app jar
2. Copy jar to project root
3. Build docker image

```shell
BUILD_NUMBER=1_0_0 ./gradlew clean assemble && cp ./build/libs/*.jar .
```
```shell
BUILD_NUMBER=1_0_0 docker build --build-arg BUILD_NUMBER=$BUILD_NUMBER . -t "hmpps-prisoner-base-location-api:local"
```
### Run a local docker image
```shell
APP=hmpps-prisoner-base-location-api && docker run --name $APP --env-file .env -p 8080:8080 -d "${APP}:local"
```

###
