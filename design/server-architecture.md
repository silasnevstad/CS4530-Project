# Husksheets Server Architecture

## Overview

The REST API server is implemented with Spark.

## Components

### Models

- `Argument`: Represents the arguments passed to the server endpoints.
- `Publisher`: Represents a publisher in the system.
- `Result`: Represents the result returned by the server.

### Services

- `PublisherService`: Handles operations related to publishers and their sheets.

### Controllers

- `HusksheetsController`: Manages the REST API endpoints and routes.

## Endpoints

The server supports the following endpoints (for now):

- `POST /api/v1/register`: Registers a new publisher.
- `GET /api/v1/getPublishers`: Retrieves all registered publishers.
- `POST /api/v1/createSheet`: Creates a new sheet for a publisher.
- `POST /api/v1/deleteSheet`: Deletes a sheet for a publisher.
- `POST /api/v1/getSheets`: Retrieves all sheets for a publisher.

## Security

The server uses Basic authentication to secure endpoints. All endpoints require an `Authorization` header with the username and password encoded in Base64.

**Note**: *For now the server just checks if the username and password are the same.*

## Logging

SLF4J Logger is used for logging server activities and errors.

## Testing

JUnit 5 is used for unit testing the server endpoints.

