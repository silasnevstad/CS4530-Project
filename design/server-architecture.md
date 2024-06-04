# Husksheets Server Architecture

## Overview

The Husksheets Server is a REST API server implemented using Spark.
The server handles operations related to publishers, sheets, updates, and subscriptions,
with data persistence managed through an SQLite database.

## Components

### Models

- `Argument`: Represents the arguments passed to the server endpoints. Fields include `publisher`, `sheet`, `id`, 
and `payload`.
- `Publisher`: Represents a publisher in the system. Only field is `name`.
- `Result`: Represents the result returned by the server. Fields include `success`, `message`, and `value` 
(a list of `Argument` objects).

### Services

- `PublisherService`: Handles operations related to publishers and their sheets. 
Manages updates and subscriptions.
Uses an SQLite database for data persistence.
- `UserService`: Manages user authentication and authorization, ensuring only authorized users can access the server.

### Database Service
`DatabaseService`: Manages the connection to the SQLite database and initializes the database schema.
Provides methods to get a database connection and set the database URL for different environments
(production vs. testing).

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

## Data Persistence

Data is persisted in an SQLite database. The database contains the following tables:
- `publishers`: Stores publisher names.
- `sheets`: Stores sheet names associated with publishers.
- `updates`: Stores updates and subscription requests for sheets, 
including a timestamp for ordering updates and a type indicating whether an update is a publication or a subscription.

## Testing

Tests are designed to run against a test (or an in-memory) database to avoid modifying the production database.

JUnit 5 is used for unit testing the server endpoints. 

Mockito is used for mocking dependencies and verifying interactions in tests.

