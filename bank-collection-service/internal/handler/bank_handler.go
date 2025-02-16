package handler

import (
	"bank-collection-service/exceptions"
	"bank-collection-service/internal/domain/model"
	"bank-collection-service/internal/services"
	"bank-collection-service/responses"
	"fmt"
	"net/http"
	"github.com/gin-gonic/gin"
)

type BankHandler struct {
	service services.BankService
}

func NewBankHandler(service services.BankService) *BankHandler {
	return &BankHandler{service: service}
}

// Create a new bank record
func (h *BankHandler) CreateBank(c *gin.Context, bank *model.UserBankList) {
	// Validate fields and return the appropriate JSON response on error
	if bank.BankCode == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Bank code is required",
		})
		return
	}
	if bank.BankName == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Bank name is required",
		})
		return
	}
	if bank.AccountHolderName == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Account holder name is required",
		})
		return
	}
	if bank.AccountNumber == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Account number is required",
		})
		return
	}

	// Call service to create bank
	if err := h.service.CreateBank(bank); err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to create bank",
			Details: err.Error(),
		})
		return
	}

	// Successful creation response
	c.JSON(http.StatusCreated, responses.SuccessResponse{
		Message: "Bank created successfully",
		Data:    bank,
	})
}

// Find bank by ID
func (h *BankHandler) GetBankById(c *gin.Context, id uint) (*model.UserBankList, error) {
	if id == 0 {
		return nil, fmt.Errorf("invalid bank ID")
	}
	bank, err := h.service.FindById(id)
	if err != nil {
		return nil, fmt.Errorf("bank not found")
	} 
	return bank, nil
}

// Find bank by account number
func (h *BankHandler) GetBankByAccountNumber(c *gin.Context, accountNumber string) (*model.UserBankList, error) {
	bank, err := h.service.FindByAccountNumber(accountNumber)
	if err != nil {
		return nil, fmt.Errorf("bank with given account number not found")
	}
	return bank, nil
}

func (h *BankHandler) GetBanksByUserId(c *gin.Context, userId uint) ([]*model.UserBankList, error) {
	banks, err := h.service.FindByUserId(userId)
	if err != nil {
		return nil, fmt.Errorf("no banks found for the user")
	}

	// Convert []model.UserBankList to []*model.UserBankList
	var bankPointers []*model.UserBankList
	for _, bank := range banks {
		bankPointers = append(bankPointers, &bank)
	}
	return bankPointers, nil
}

func (h *BankHandler) DeleteBanksByIds(c *gin.Context, ids []uint) error {
	if len(ids) == 0 {
		return fmt.Errorf("provide a valid list of IDs for deletion")
	}

	if err := h.service.DeleteByIds(ids); err != nil {
		return fmt.Errorf("failed to delete banks: %v", err)
	}
	return nil
}
