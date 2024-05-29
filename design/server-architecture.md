# Husksheets Server Architecture

## Overview

The Husksheets Server is a REST API server is implemented using Spark.

## Components

### Models

- `Argument`: Represents the arguments passed to the server endpoints. Fields include `publisher`, `sheet`, `id`, 
and `payload`.
- `Publisher`: Represents a publisher in the system. Only field is `name`.
- `Result`: Represents the result returned by the server. Fields include `success`, `message`, and `value` 
(a list of `Argument` objects).

### Services

- `PublisherService`: Handles operations related to publishers and their sheets. Manages updates and subscriptions.
- `UserService`: Manages user authentication and authorization.

### Controllers

- `HusksheetsController`: Manages the REST API endpoints and routes. Handling requests and responses, 
ensuring proper authentication and invoking the appropriate service methods.

## Endpoints

The server supports the following endpoints (for now):

- `POST /api/v1/register`: Registers a new publisher.
- `GET /api/v1/getPublishers`: Retrieves all registered publishers.
- `POST /api/v1/createSheet`: Creates a new sheet for a publisher.
- `POST /api/v1/deleteSheet`: Deletes a sheet for a publisher.
- `POST /api/v1/getSheets`: Retrieves all sheets for a publisher.
- `POST /api/v1/updatePublished`: Updates a sheet that the client has published.
- `POST /api/v1/updateSubscription`: Updates a subscription (request) for a sheet.
- `POST /api/v1/getUpdatesForPublished`: Retrieves updates for a sheet that the client has published.
- `POST /api/v1/getUpdatesForSubscription`: Retrieves updates for a sheet that the client is subscribed to.

## Security

The server uses Basic authentication to secure endpoints. 
All endpoints require an `Authorization` header with the username and password encoded in Base64.
The set of client names and passwords is pre-assigned.

## Testing

JUnit 5 is used for unit testing the server endpoints. 
Mockito is used for mocking dependencies and verifying interactions in tests.

