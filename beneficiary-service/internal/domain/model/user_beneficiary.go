package model

import (
	"time"
)

type UserBeneficiary struct {
	ID                uint           `gorm:"primaryKey;autoIncrement" json:"id"`
	BankCode          string         `gorm:"type:varchar(255)" json:"bankCode,omitempty" binding:"required"`
	BankName          string         `gorm:"type:varchar(255)" json:"bankName,omitempty" binding:"required"`
	AccountHolderName string         `gorm:"type:varchar(255)" json:"accountHolderName,omitempty" binding:"required"`
	AccountNumber     string         `gorm:"type:varchar(255);unique;not null" json:"accountNumber" binding:"required"`
	UserID            uint           `json:"-"`
	CreatedOn         time.Time      `gorm:"autoCreateTime" json:"-"`
	UpdatedOn         time.Time      `gorm:"autoUpdateTime" json:"-"`
}
