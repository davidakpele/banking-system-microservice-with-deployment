package pesco.authentication_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pesco.authentication_service.enums.UserStatus;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private Users user;
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
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String referralCode;
    private String totalReferers;
    private String referralLink;
    private String photo;
}
