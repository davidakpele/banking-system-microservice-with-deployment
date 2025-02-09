package routers

import (
    "bank-collection-service/controller"
    "bank-collection-service/middleware"
    "github.com/gin-gonic/gin"
)

// RegisterRoutes initializes the routes for the application
func RegisterRoutes(router *gin.Engine, base64Secret string, bankController *controller.BankController) {

    // Private Routes (requires JWT authentication)
    privateRoutes := router.Group("/")
    privateRoutes.Use(middleware.AuthenticationMiddleware(base64Secret))
    {
        privateRoutes.GET("/", bankController.DefaultHome)
        privateRoutes.POST("/bank/create", bankController.CreateBankList)
        privateRoutes.GET("/bank/details/:id", bankController.GetBankById)
        privateRoutes.GET("/bank/accounts/:accountNumber", bankController.GetBankByAccountNumber)
        privateRoutes.GET("/bank/users/:id", bankController.GetBanksByUserId)
        privateRoutes.DELETE("/bank/delete/:id", bankController.DeleteBanksByIds)
    }
}