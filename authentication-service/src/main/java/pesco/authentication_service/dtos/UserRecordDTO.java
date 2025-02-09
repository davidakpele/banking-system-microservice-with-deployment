package pesco.authentication_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pesco.authentication_service.models.UserRecord;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRecordDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String telephone;
    private String gender;
    private String country;
    private String city;
    private String nextOfKing;
    private boolean isTransferPinSet;
    private boolean locked;
    private LocalDateTime lockedAt;
    private boolean isBlocked;
    private Long blockedDuration;
    private String blockedUntil;
    private String blockedReason;
    private String referralCode;
    private String totalReferers;
    private String referralLink;
    private UserDTO userDTO;
    private String photo;

    public static UserRecordDTO fromEntity(UserRecord record) {
        return UserRecordDTO.builder()
            .id(record.getId())
            .firstName(record.getFirstName())
            .lastName(record.getLastName())
            .gender(record.getGender())
            .country(record.getCountry())
            .city(record.getCity())
            .nextOfKing(record.getNextOfKing())
            .isTransferPinSet(record.isTransferPinSet())
            .locked(record.isLocked())
            .lockedAt(record.getLockedAt())
            .referralCode(record.getReferralCode())
            .isBlocked(record.isBlocked())
            .blockedDuration(record.getBlockedDuration())
            .blockedUntil(record.getBlockedUntil())
            .blockedReason(record.getBlockedReason())
            .totalReferers(record.getTotalReferers())
            .referralLink(record.getReferralLink())
            .photo(record.getPhoto())
            .build();
    }
}
