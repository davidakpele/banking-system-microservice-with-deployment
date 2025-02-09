package main

import (
    "log"
    "os"
    "bank-collection-service/db"
    "bank-collection-service/internal/repository"
    "bank-collection-service/internal/services"
    "bank-collection-service/config"
    "bank-collection-service/migrations"
    "bank-collection-service/internal/handler"    // Import handler
    "bank-collection-service/controller" // Import controller
    "bank-collection-service/routers" // Import router package
    "github.com/gin-gonic/gin"
    "github.com/gin-contrib/cors"
    "github.com/joho/godotenv"
)

func main() {
    // Load .env file
    if err := godotenv.Load(); err != nil {
        log.Fatal("Error loading .env file")
    }

    // Load configuration
    cfg := config.LoadConfig()

    // Connect to the database
    database, err := db.ConnectDatabase(cfg)
    if err != nil {
        log.Fatalf("Failed to connect to database: %v", err)
    }

    // Migrate model
    if err := migrations.MigrateModels(database); err != nil {
        log.Fatalf("Database migration failed: %v", err)
    }

    // Retrieve the JWT secret key from the environment variable
    jwtSecretKey := os.Getenv("JWT_SECRET_KEY")
    if jwtSecretKey == "" {
        log.Fatal("JWT_SECRET_KEY is not set in .env file")
    }

    // Create router
    router := gin.Default()

    // CORS configuration
    router.Use(cors.Default())

    // Initialize dependencies
    bankRepo := repository.NewBankRepository(database) 
    bankService := services.NewBankService(bankRepo)
    bankHandler := handler.NewBankHandler(bankService)
    bankController := controller.NewBankController(bankHandler)

    // Register all routes by passing the router and dependencies
    routers.RegisterRoutes(router, jwtSecretKey, bankController) 

    // Start the server
    if err := router.Run(":8040"); err != nil {
        log.Fatalf("Error starting server: %v", err)
    }
    gin.SetMode(gin.ReleaseMode)
}
