package routers

import (
    "beneficiary-service/controller"
    "beneficiary-service/middleware"
    "github.com/gin-gonic/gin"
)

// RegisterRoutes initializes the routes for the application
func RegisterRoutes(router *gin.Engine, base64Secret string, userBeneficiaryController *controller.UserBeneficiaryController) {

    // Private Routes (requires JWT authentication)
    privateRoutes := router.Group("/")
    privateRoutes.Use(middleware.AuthenticationMiddleware(base64Secret))
    {
        privateRoutes.GET("/", userBeneficiaryController.DefaultHome)
        privateRoutes.POST("/create", userBeneficiaryController.CreateBeneficiary)
        privateRoutes.GET("/beneficiary/:userID", userBeneficiaryController.GetBeneficiaryByUserID)
        privateRoutes.GET("/beneficiary/all/:userID", userBeneficiaryController.GetAllBeneficiariesByUserID)
        privateRoutes.DELETE("/delete", userBeneficiaryController.DeleteBeneficiariesByIDs)
    }
}