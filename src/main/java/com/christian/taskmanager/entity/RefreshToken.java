package com.christian.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token_token", columnList = "token"),
                @Index(name = "idx_refresh_token_user", columnList = "user_id"),
                @Index(name = "idx_refresh_token_expiry", columnList = "expiry_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    private boolean revoked;

    private String deviceName;

    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
