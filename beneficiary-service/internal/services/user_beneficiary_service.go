package services

import (
	"beneficiary-service/internal/domain/model"
	"beneficiary-service/internal/repository"
	"errors"
)

type UserBeneficiaryService interface {
	GetBeneficiaryByUserID(userID uint) (*model.UserBeneficiary, error)
	GetAllBeneficiariesByUserID(userID uint) ([]model.UserBeneficiary, error)
	DeleteBeneficiariesByIDs(ids []uint) error
	CreateBeneficiary(beneficiary *model.UserBeneficiary) error
}

type userBeneficiaryService struct {
	repo repository.UserBeneficiaryRepository
}

func NewUserBeneficiaryService(repo repository.UserBeneficiaryRepository) UserBeneficiaryService {
	return &userBeneficiaryService{repo: repo}
}

func (s *userBeneficiaryService) GetBeneficiaryByUserID(userID uint) (*model.UserBeneficiary, error) {
	beneficiary, err := s.repo.FindByUserID(userID)
	if err != nil {
		return nil, errors.New("beneficiary not found")
	}
	return beneficiary, nil
}

func (s *userBeneficiaryService) GetAllBeneficiariesByUserID(userID uint) ([]model.UserBeneficiary, error) {
	return s.repo.FindAllUserBeneficiaries(userID)
}

func (s *userBeneficiaryService) DeleteBeneficiariesByIDs(ids []uint) error {
	return s.repo.DeleteUserBeneficiariesByIDs(ids)
}

// CreateBeneficiary handles the creation of a new beneficiary
func (s *userBeneficiaryService) CreateBeneficiary(beneficiary *model.UserBeneficiary) error {
	existingBeneficiary, err := s.repo.FindByAccountNumber(beneficiary.AccountNumber)
	if err == nil && existingBeneficiary != nil {
		return errors.New("beneficiary with this account number already exists")
	}

	// Save the beneficiary
	err = s.repo.SaveBeneficiary(beneficiary)
	if err != nil {
		return errors.New("failed to create beneficiary")
	}

	return nil
}
