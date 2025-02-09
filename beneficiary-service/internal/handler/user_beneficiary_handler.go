package handler

import (
	"beneficiary-service/exceptions"
	"beneficiary-service/internal/domain/model"
	"beneficiary-service/internal/services"
	"net/http"
	"github.com/gin-gonic/gin"
)

type UserBeneficiaryHandler struct {
	service services.UserBeneficiaryService
}

func NewUserBeneficiaryHandler(service services.UserBeneficiaryService) *UserBeneficiaryHandler {
	return &UserBeneficiaryHandler{service: service}
}

// CreateBeneficiary handles creating a new beneficiary for a user
func (h *UserBeneficiaryHandler) CreateBeneficiary(c *gin.Context, beneficiary *model.UserBeneficiary) error {
	err := h.service.CreateBeneficiary(beneficiary)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to create beneficiary",
			Details: err.Error(),
		})
		return err
	}
	c.JSON(http.StatusOK, gin.H{"message": "Beneficiary created successfully"})
	return nil
}

// GetBeneficiaryByUserID retrieves a single beneficiary by user ID
func (h *UserBeneficiaryHandler) GetBeneficiaryByUserID(c *gin.Context, userID uint) (*model.UserBeneficiary, error) {
	beneficiary, err := h.service.GetBeneficiaryByUserID(userID)
	if err != nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "Beneficiary not found",
			Details: err.Error(),
		})
		return nil, err
	}
	return beneficiary, nil
}

// GetAllBeneficiariesByUserID retrieves all beneficiaries for a user
func (h *UserBeneficiaryHandler) GetAllBeneficiariesByUserID(c *gin.Context, userID uint) ([]model.UserBeneficiary, error) {
	beneficiaries, err := h.service.GetAllBeneficiariesByUserID(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to retrieve beneficiaries",
			Details: err.Error(),
		})
		return nil, err
	}
	return beneficiaries, nil
}

// DeleteBeneficiariesByIDs handles deleting multiple beneficiaries by their IDs
func (h *UserBeneficiaryHandler) DeleteBeneficiariesByIDs(c *gin.Context, ids []uint) error {
	err := h.service.DeleteBeneficiariesByIDs(ids)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to delete beneficiaries",
			Details: err.Error(),
		})
		return err
	}
	c.JSON(http.StatusOK, gin.H{"message": "Beneficiaries deleted successfully"})
	return nil
}
