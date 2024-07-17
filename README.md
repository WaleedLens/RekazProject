

## Rekaz Simple Drive: Simple Object Storage Service
![Screenshot from 2024-07-15 23-06-27](https://github.com/user-attachments/assets/5a81c7ea-e68e-4a8f-9d4b-6b796e934243)


### Introduction

The Rekaz Simple Drive is a robust and flexible object storage service designed to handle the storage and retrieval of files or blobs of data. It offers a unified interface to interact with multiple storage backends, abstracting the complexities of individual storage solutions.

### Key Features

* **Unified Storage Interface:** Store and retrieve data seamlessly across various backends.
* **Supported Backends:**
    * Amazon S3 Compatible Storage (using only HTTP)
    * Database Storage
    * Local File System Storage
    * FTP Storage
* **RESTful API:** Intuitive endpoints for data management.
* **Bearer Token Authentication:** Secure your API access.
* **Metadata Tracking:** Maintain essential information about stored blobs.

### Tech Stack

* **Core Language:** Java
* **Database:** MongoDB
* **Infrastructure/Deployment:** Docker, Shell Scripting

### Libraries Used

| Library Name         | Description                                                      | Why I Used It                                                                                                             |
|-----------------------|------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| Undertow Core         | A high-performance, non-blocking web server for Java.          | Lightweight and efficient, ideal for building microservices and RESTful APIs.                                             |
| SLF4J API             | A simple logging facade for Java.                                | Provides a common interface for various logging frameworks, enabling flexibility in logging implementation.                |
| Logback Classic       | An SLF4J implementation with enhanced logging capabilities.    | Offers advanced features like configuration files, filtering, and multiple appenders.                                     |
| Google Guice          | A dependency injection framework for Java.                      | Simplifies dependency management and promotes testability.                                                                |
| Jackson Core/Databind | Libraries for processing JSON data in Java.                       | Used for parsing and generating JSON for requests and responses.                                                        |
| Reflections           | A Java library for scanning classpath metadata.                  | Used for automatic discovery of API endpoints and their annotations.                                                     |
| dotenv-java           | A library for loading environment variables from a `.env` file. | Simplifies configuration management by loading settings from a `.env` file.                                               |
| MongoDB Driver Sync  | Official Java driver for MongoDB (synchronous version).        | Provides seamless integration with MongoDB databases for data storage and retrieval.                                       |
| Apache HttpClient5   | A library for HTTP communication.                                | Used for making HTTP requests to S3-compatible storage and FTP servers.                                                    |
| Apache Commons Net   | A library for various network protocols (including FTP).       | Used for FTP communication when the FTP storage backend is selected.                                                       |
| JUnit 5               | Testing framework for Java.                                   | Enables comprehensive unit and integration testing for the project.                                                        |
| MockFtpServer         | A mock FTP server for testing purposes.                         | Used for simulating an FTP server during unit tests.                                                                        |
| Mockito               | A mocking framework for Java.                                   | Used for creating mock objects and stubbing their behavior in unit tests.                                                  |
| JJWT                  | A library for creating and verifying JSON Web Tokens (JWTs).    | Used for implementing secure Bearer token authentication.                                                                |


### Amazon S3 Protocol Implementation

I've implemented direct interaction with S3-compatible storage services using only standard Java libraries. This involves crafting HTTP requests (PUT, GET) to the S3 API endpoints, handling authentication (signatures).
Check /aws

### Authentication Implementation

It is a Bearer token authentication with JSON Web Tokens (JWTs) for enhanced security. Authentication is based on RSA keys for signing and verifying tokens.
Check /authentication


### How to Build and Run

#### Requirements

Before running the project, please ensure you have the following prerequisites:

| Software                     | Version              | Installation Notes                                                               |
| ---------------------------- | -------------------- | ---------------------------------------------------------------------------------- |
| Java Development Kit (JDK) | 21+                  | Download and install from the official Oracle website or use a package manager. |
| Apache Maven                | (Latest Stable)      | Install from the official Apache Maven website or use a package manager.        |
| Docker                      | (Latest Stable)      | Download and install from the official Docker website.                            |
| Docker Compose              | (Latest Stable)      | Included with Docker Desktop or install separately on Linux.                      |



You need to run ./build.sh and make sure it is executable using {chmod +x build.sh} command.  Build.sh will generate your jar file & RSA key pair.
1. **Generate Keys & Jar file (One-Time Setup):** Run the provided shell script to generate your RSA key pair for JWTs:
   ```bash
   ./build.sh
   ```
   

2. **Configuration (.env file):** Create a `.env` file in the project root directory with the following variables (replace placeholders with your values):
   
```
# -----------------------------------
# Rekaz Simple Drive Configuration
# -----------------------------------

# Server Configuration
PORT=8906           # The port on which the server will listen.
HOST=localhost       # The hostname or IP address the server will bind to.

# Storage Backend (Choose ONE)
STORAGE_BACKEND=database  # Options: database, s3, local, ftp

# -----------------------------------
# Backend-Specific Configurations
# -----------------------------------

# Database (MongoDB)
DB_HOST=your_mongodb_host          # The hostname or IP address of your MongoDB server.
DB_PORT=27017                   # The port on which your MongoDB server is listening.
DB_NAME=your_database_name        # The name of the MongoDB database to use.
DB_USER=your_username             # The username for authenticating with MongoDB.
DB_PASSWORD=your_password         # The password for authenticating with MongoDB.
AUTH_SOURCE=?authSource=admin     # The authentication database (usually 'admin').

# AWS S3
S3_ACCESS_KEY=your_s3_access_key   # Your AWS S3 access key.
S3_SECRET_KEY=your_s3_secret_key   # Your AWS S3 secret key.
S3_REGION=your_s3_region          # The AWS region your S3 bucket is in.
S3_BUCKET=your_s3_bucket_name     # The name of your S3 bucket.

# Local File System
LOCAL_STORAGE_PATH=/path/to/your/local/storage  # The absolute path to the local directory for storing files.

# FTP
FTP_HOST=your_ftp_host          # The hostname or IP address of your FTP server.
FTP_PORT=your_ftp_port          # The port on which your FTP server is listening (usually 21).
FTP_USER=your_ftp_username       # The username for authenticating with the FTP server.
FTP_PASSWORD=your_ftp_password   # The password for authenticating with the FTP server.

# -----------------------------------
# Security (RSA Keys)
# -----------------------------------

PRIVATE_KEY_PATH=private_key.der # The path to your RSA private key file (in DER format).
PUBLIC_KEY_PATH=public_key.der  # The path to your RSA public key file (in DER format).
```


3. **Build Docker Image:**
   ```bash
   docker build -t your-image-name . 
   ```

4. **Run with Docker Compose:**
   ```bash
   docker compose up
   ```

### Database

This application utilizes MongoDB for efficient data storage. Ensure a MongoDB instance is running and accessible. You do not need to create collections manually; the application automatically handles data management.

**Remember: You need to create a database.**

#### Data Model
The application's data is structured into two primary collections:

**Metadata**
Stores information about your data:

```json
{
  "id": "unique identifier",
  "size": "data size in bytes",
  "createdAt": "ISO 8601 timestamp (e.g., 2024-07-17T09:18:00Z)"
}
```
**Blob**
Stores the actual binary data (files):

```json
{
  "id": "unique identifier",
  "data": "file content"
}
```

### API Endpoints

**Authenticated Endpoints (Require Bearer Token):**

* **POST /v1/blobs:** Save a blob of data.
   * Request Header:
      ```
      Authorization: Bearer <jwt_token>
      ```
   * Request Body:
      ```json
      {
          "id": "unique_blob_id",
          "data": "base64_encoded_data"
      }
      ```
   * Response (201 Created):

* **GET /v1/blobs/{id}:** Retrieve a blob by ID.
   * Request Header:
      ```
      Authorization: Bearer <jwt_token>
      ```

   * Response (200 OK):
      ```json
      {
          "id": "unique_blob_id",
          "data": "base64_encoded_data",
          "size": "size in bytes",
          "created_at": "2024-07-13T23:45:25Z"
      }
       ```

**Unauthenticated Endpoint:**

* **GET /v1/auth/jwt:** Get Authorization Token JWT.

  * Response (200 OK):
      ```json
      {
      "subject": "some_subject",
      "token": "jwt_token",
          
      }
       ```

 ## System Analysis:
 ### Backend Storage 
![image](https://github.com/user-attachments/assets/3bc9510c-1f5c-4869-8303-f17e9fec4e91)

### Backend AWS S3:
![image](https://github.com/user-attachments/assets/f89b4fae-0a59-43e3-9371-fa979fa0cd7a)

### Authentication Flow
![Screenshot from 2024-07-17 09-08-06](https://github.com/user-attachments/assets/2e52eb9b-3062-4169-8395-a6ac5f049812)





