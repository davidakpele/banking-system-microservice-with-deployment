package main

import (
    "log"
    "os"
    "blacklist-service/db"
    "blacklist-service/internal/repository"
    "blacklist-service/internal/services"
    "blacklist-service/config"
    "blacklist-service/migrations"
    "blacklist-service/internal/handler"
    "blacklist-service/controller"
    "blacklist-service/routers" 
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
    blacklistedWalletRepo := repository.NewBlackListedWalletRepository(database) 
    blacklistedWalletService := services.NewBlackListedWalletService(blacklistedWalletRepo)
    blacklistedWalletHandler := handler.NewBlackListedWalletHandler(blacklistedWalletService)
    bankController := controller.NewBlackListedWalletController(blacklistedWalletHandler)

    // Register all routes by passing the router and dependencies
    routers.RegisterRoutes(router, jwtSecretKey, bankController) 

    // Start the server
    if err := router.Run(":8082"); err != nil {
        log.Fatalf("Error starting server: %v", err)
    }
    gin.SetMode(gin.ReleaseMode)
}
