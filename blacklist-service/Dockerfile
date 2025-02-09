# Use the official Golang image as a base image
FROM golang:1.23-alpine as builder

# Set the Current Working Directory inside the container
WORKDIR /app

# Copy go.mod and go.sum files to download dependencies
COPY go.mod go.sum ./

# Download all the dependencies. Dependencies are cached if the go.mod and go.sum are not changed
RUN go mod tidy

# Copy the source code into the container
COPY . .

# Build the Go app
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /bin/app

# Start a new stage from the official Alpine image
FROM alpine:latest  

# Install necessary dependencies (if required)
RUN apk --no-cache add ca-certificates curl

# Set the Current Working Directory inside the container
WORKDIR /root/

# Copy the pre-built binary file from the builder image
COPY --from=builder /app/blacklist-service .

# Expose the port that the application will run on
EXPOSE 8082

# Command to run the executable
CMD ["./blacklist-service"]
