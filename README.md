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

There is also a `docker-compose.yml` that can be used to run a local instance of the template in docker and also an
instance of HMPPS Auth (required if your service calls out to other services using a token).

This API application depends on several services to run.

| Dependency            | Description                                   | Default | Override Env Var                                        |
|---------------------  |-----------------------------------------------|---------|---------------------------------------------------------|
| hmpps-auth            | OAuth2 API server for authenticating requests |         | `HMPPS_AUTH_URL`                                        |
| Prisoner Search API   | API for retrieving prisoner profile           |         | `API_CLIENT_CLIENTS_PRISONER-OFFENDER-SEARCH_BASE-URL`  |

```bash
docker compose pull && docker compose up -d
```

will build the application and run it and HMPPS Auth within a local docker instance.

### Environment variables
Defining env var for *local* run

| Env. var.           | description                                         |
|---------------------|-----------------------------------------------------|
| `API_CLIENT_ID`     | API client ID for accessing Prisoner Search API     |
| `API_CLIENT_SECRET` | API client secret for accessing Prisoner Search API |
_*_ These values can be obtained from k8s secrets in `dev` env.

These can be set in a `.env` file, e.g.
```dotenv
HMPPS_AUTH_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth
API_CLIENT_ID=xxx
API_CLIENT_SECRET=xxx
API_CLIENT_CLIENTS_PRISONER-OFFENDER-SEARCH_BASE-URL=https://prisoner-search-dev.prison.service.justice.gov.uk
```

### Running the application in Intellij

```bash
docker compose pull && docker compose up --scale hmpps-prisoner-base-location-api=0 -d
```


will just start a docker instance of HMPPS Auth. The application should then be started with a `dev` active profile
in Intellij.

