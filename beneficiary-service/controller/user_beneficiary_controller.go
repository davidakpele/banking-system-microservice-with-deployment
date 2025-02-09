package controller

import (
	"beneficiary-service/exceptions"
	"beneficiary-service/internal/domain/model"
	"beneficiary-service/internal/handler"
	"beneficiary-service/payloads"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type UserBeneficiaryController struct {
	handler *handler.UserBeneficiaryHandler
}

func NewUserBeneficiaryController(handler *handler.UserBeneficiaryHandler) *UserBeneficiaryController {
	return &UserBeneficiaryController{handler: handler}
}

// CreateBeneficiary handles creating a new beneficiary for a user
func (ubc *UserBeneficiaryController) CreateBeneficiary(c *gin.Context) {
	var payload payloads.BankAccountRequest
	if err := c.ShouldBindJSON(&payload); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}

	// Construct the beneficiary model
	beneficiary := model.UserBeneficiary{
		UserID: payload.UserID,
		BankCode:        payload.BankCode,
		BankName:        payload.BankName, 
		AccountHolderName: payload.AccountHolderName,
		AccountNumber:   payload.AccountNumber,
	}

	// Call the handler
	err := ubc.handler.CreateBeneficiary(c, &beneficiary)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to create beneficiary",
			Details: err.Error(),
		})
		return
	}
}

// GetBeneficiaryByUserID retrieves a single beneficiary by user ID
func (ubc *UserBeneficiaryController) GetBeneficiaryByUserID(c *gin.Context) {
	userIDStr := c.Param("userID")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a valid number",
		})
		return
	}

	// Call the handler to get the beneficiary by user ID
	beneficiary, err := ubc.handler.GetBeneficiaryByUserID(c, uint(userID))
	if err != nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "Beneficiary not found",
			Details: "No beneficiary found for this user",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Success", "data": beneficiary})
}

// GetAllBeneficiariesByUserID retrieves all beneficiaries for a user
func (ubc *UserBeneficiaryController) GetAllBeneficiariesByUserID(c *gin.Context) {
	userIDStr := c.Param("userID")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a valid number",
		})
		return
	}

	// Call the handler to get all beneficiaries by user ID
	beneficiaries, err := ubc.handler.GetAllBeneficiariesByUserID(c, uint(userID))
	if err != nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "No beneficiaries found",
			Details: "No beneficiaries found for this user",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Success", "data": beneficiaries})
}

// DeleteBeneficiariesByIDs handles deleting multiple beneficiaries by their IDs
func (ubc *UserBeneficiaryController) DeleteBeneficiariesByIDs(c *gin.Context) {
	var payload struct {
		IDs []uint `json:"ids"`
	}

	if err := c.ShouldBindJSON(&payload); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}

	// Call the handler to delete the beneficiaries by IDs
	err := ubc.handler.DeleteBeneficiariesByIDs(c, payload.IDs)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to delete beneficiaries",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Beneficiaries deleted successfully"})
}

// defaultHome handles the default home route
func (bc *UserBeneficiaryController) DefaultHome(c *gin.Context) {
    // Create a simple response
    response := gin.H{
        "message": "Welcome to the Beneficiary Service!",
        "status":  "success",
    }
    
    // Return JSON response
    c.JSON(http.StatusOK, response)
}