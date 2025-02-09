package repository

import (
	"beneficiary-service/internal/domain/model"
	"gorm.io/gorm"
)

type UserBeneficiaryRepository interface {
	FindByUserID(userID uint) (*model.UserBeneficiary, error)
	FindAllUserBeneficiaries(userID uint) ([]model.UserBeneficiary, error)
	DeleteUserBeneficiariesByIDs(ids []uint) error
	FindByAccountNumber(accountNumber string) (*model.UserBeneficiary, error)
	SaveBeneficiary(beneficiary *model.UserBeneficiary) error
}

type userBeneficiaryRepository struct {
	db *gorm.DB
}

func NewUserBeneficiaryRepository(db *gorm.DB) UserBeneficiaryRepository {
	return &userBeneficiaryRepository{db: db}
}

func (r *userBeneficiaryRepository) FindByUserID(userID uint) (*model.UserBeneficiary, error) {
	var beneficiary model.UserBeneficiary
	err := r.db.Where("user_id = ?", userID).First(&beneficiary).Error
	if err != nil {
		return nil, err
	}
	return &beneficiary, nil
}

func (r *userBeneficiaryRepository) FindAllUserBeneficiaries(userID uint) ([]model.UserBeneficiary, error) {
	var beneficiaries []model.UserBeneficiary
	err := r.db.Where("user_id = ?", userID).Find(&beneficiaries).Error
	if err != nil {
		return nil, err
	}
	return beneficiaries, nil
}

func (r *userBeneficiaryRepository) DeleteUserBeneficiariesByIDs(ids []uint) error {
	return r.db.Where("id IN ?", ids).Delete(&model.UserBeneficiary{}).Error
}

// FindByAccountNumber retrieves a beneficiary by their account number
func (r *userBeneficiaryRepository) FindByAccountNumber(accountNumber string) (*model.UserBeneficiary, error) {
	var beneficiary model.UserBeneficiary
	err := r.db.Where("account_number = ?", accountNumber).First(&beneficiary).Error
	if err != nil {
		return nil, err
	}
	return &beneficiary, nil
}

// SaveBeneficiary saves a new beneficiary to the database
func (r *userBeneficiaryRepository) SaveBeneficiary(beneficiary *model.UserBeneficiary) error {
	err := r.db.Create(beneficiary).Error
	if err != nil {
		return err
	}
	return nil
}
