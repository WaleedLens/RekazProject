#!/bin/bash


# Generate a private key
openssl genpkey -algorithm RSA -out private_key.pem

# Extract the public key
openssl rsa -pubout -in private_key.pem -out public_key.pem

# Convert the private key to DER format
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt

# Convert the public key to DER format
openssl rsa -pubin -inform PEM -outform DER -in public_key.pem -out public_key.der


# Run the Maven command to build the project
mvn clean package


