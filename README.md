# quick-note-api-spring-firebase

![Java](https://img.shields.io/badge/Java-17%2F21-007396?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Admin%20SDK-FFCA28?logo=firebase&logoColor=black)
![Google Cloud](https://img.shields.io/badge/Google%20Cloud-Firestore%20%26%20Cloud%20Run-4285F4?logo=googlecloud&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)

## Project Overview

This repository contains the Spring Boot backend for the Quick-Note Polyglot platform. It represents **Stack 3** of the broader system and serves as a decoupled, serverless-friendly REST API built on a NoSQL document database. The service is designed to provide note management and authentication capabilities through the Firebase Admin SDK and Google Cloud Firestore.

The architecture intentionally favors a lightweight, cloud-native approach over traditional relational persistence patterns. The implementation is optimized for stateless execution, container deployment, and integration with a separate frontend client.

## The Polyglot Ecosystem

Quick-Note Polyglot is composed of multiple backend options and user interfaces that together demonstrate technology diversity within a common product domain. This repository is one backend implementation in that ecosystem, focused on a Java and Spring Boot stack backed by Firestore.

The corresponding frontend application is available at: https://github.com/DineshMoorthy007/quick-note-react-ui

## System Architecture & Design Patterns

This repository deliberately departs from conventional Spring Data JPA and Hibernate usage. Instead of modeling persistence around entities, repositories, and relational mappings, it uses the native Firebase Admin SDK to interact directly with Google Cloud Firestore collections and documents.

This design has several implications:

- Data is modeled as document-oriented Java POJOs and DTOs rather than JPA entities.
- Persistence operations are executed through Firestore collection and document references.
- Reads and writes are asynchronous at the SDK level through `ApiFuture`, which must be resolved explicitly.
- The service layer is responsible for coordinating serialization, retrieval, mutation, and error handling.

In practical terms, the application performs NoSQL operations in a Java-centric way while preserving the document model of Firestore. Each Firestore call returns an `ApiFuture`, and the application resolves those futures using `get()` in the service layer. This ensures deterministic completion handling and allows `InterruptedException` and `ExecutionException` to be handled in a controlled manner.

The repository also demonstrates strict JSON deserialization discipline. Primitive Java types such as `boolean` are not safe when a frontend may omit a field or submit `null`. To prevent Jackson deserialization failures, wrapper types such as `Boolean` are used where nullability must be tolerated. This is especially relevant for note pinning state, where frontend payloads may omit the field or send a null value during creation.

From an architectural perspective, the stack is best understood as:

- Spring Boot as the HTTP and dependency injection layer.
- Firebase Admin SDK as the persistence client.
- Firestore as the document database.
- DTOs and POJOs as the explicit boundary between HTTP payloads and database documents.
- Service classes as the transaction and orchestration layer.

## API Contract

The application exposes seven REST endpoints. The table below defines the public contract.

| Method | Endpoint | Purpose | Request Body / Parameters | Response |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/login` | Authenticate an existing user | `AuthRequestDTO` with `username` and `password` | `AuthResponseDTO` |
| POST | `/api/auth/register` | Register a new user | `AuthRequestDTO` with `username` and `password` | `AuthResponseDTO` |
| GET | `/api/notes` | Retrieve notes for the authenticated user | `Authorization: Bearer <token>` header | List of `Note` objects |
| POST | `/api/notes` | Create a new note | `Authorization: Bearer <token>` header and `NoteRequestDTO` body | Created `Note` |
| PUT | `/api/notes/{id}` | Update an existing note | `Authorization: Bearer <token>` header, path variable `id`, and `NoteRequestDTO` body | Updated `Note` |
| PUT | `/api/notes/{id}/pin` | Toggle the pin state of a note | `Authorization: Bearer <token>` header and path variable `id` | Updated `Note` |
| DELETE | `/api/notes/{id}` | Delete a note | `Authorization: Bearer <token>` header and path variable `id` | JSON payload containing `message` and `id` |

## Prerequisites

Before running the application locally, ensure the following tools are installed on the host machine:

- Java 17 or Java 21
- Maven 3.9 or later

The project is configured with a Maven Wrapper, so the preferred execution path is to use the wrapper provided in the repository.

## Security & Credentials

This application requires access to Google Cloud credentials in order to initialize the Firebase Admin SDK. A `firebase-admin-key.json` service account file is required for local development.

There are two supported ways to provide the credential file:

1. Set the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the absolute filesystem path of `firebase-admin-key.json`.
2. Place `firebase-admin-key.json` directly in `src/main/resources/` so it is available on the application classpath.

The first approach is preferred for local development because it keeps secrets outside the application bundle. The second approach is acceptable for controlled local testing, but it should never be treated as a production credential strategy.

Regardless of the method used, the credential file must grant access to Firestore and be kept out of source control.

## Local Execution

To start the application locally, use the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows, the equivalent command is:

```powershell
mvnw.cmd spring-boot:run
```

## Containerization & Cloud Deployment

This stack is optimized for deployment as a serverless container on Google Cloud Run. The runtime model aligns well with Firestore-backed stateless APIs because request handling does not depend on in-memory session state or local disk persistence. The application also uses a multi-stage Docker build to keep the final runtime image lightweight, reproducible, and suitable for cloud-native deployment. The first stage performs the Maven build, while the second stage packages only the compiled Spring Boot artifact on a compact Eclipse Temurin JRE base image.

A containerized Cloud Run deployment supports:

- Horizontal scaling based on demand
- Minimal operational overhead
- Separation of build and runtime concerns
- Straightforward integration with Google Cloud identity and secret management

For enterprise environments, this deployment model provides a pragmatic balance between operational simplicity and cloud-native scalability.

```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

For production deployment to Google Cloud Run, use the following command:

```bash
gcloud run deploy api-spring-firebase --source . --region asia-south1 --allow-unauthenticated --port 8080
```

## Implementation Notes

- Notes and users are modeled as plain Java objects rather than JPA-managed entities.
- Firestore collections are accessed through the Firebase Admin SDK.
- ApiFuture results are resolved in the service layer to preserve deterministic behavior.
- The API contract is designed to remain stable for the React frontend client.
- CORS is restricted to the frontend origin `http://localhost:5173` during local development.

## Repository Scope

This repository is intentionally narrow in scope. It focuses on the backend responsibilities of authentication, note persistence, and API governance. Presentation logic, routing, and user interaction are handled by the companion frontend application.

## License

No license has been declared in this repository.
