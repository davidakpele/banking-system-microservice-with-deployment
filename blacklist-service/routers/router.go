package routers

import (
    "blacklist-service/controller"
    "blacklist-service/middleware"
    "github.com/gin-gonic/gin"
)

// RegisterRoutes initializes the routes for the application
func RegisterRoutes(router *gin.Engine, base64Secret string, blacklistedController *controller.BlackListedWalletController) {

    // Private Routes (requires JWT authentication)
    privateRoutes := router.Group("/")
    privateRoutes.Use(middleware.AuthenticationMiddleware(base64Secret))
    {
        privateRoutes.GET("/", blacklistedController.DefaultHome)
        privateRoutes.GET("/blacklist/status/:walletID", blacklistedController.CheckWalletBlacklistStatus)
        privateRoutes.DELETE("/blacklist/delete/:walletID", blacklistedController.RemoveBlacklistedWallet)
        privateRoutes.POST("/blacklist/add", blacklistedController.AddToBlackList)
    }
}