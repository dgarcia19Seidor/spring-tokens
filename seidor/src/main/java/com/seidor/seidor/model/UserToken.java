package com.seidor.seidor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "USERS_TOKEN",
        uniqueConstraints = @UniqueConstraint(columnNames = "token")
)
@Getter
@Setter
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mail_base64", length = 512, nullable = false)
    private String mailBase64;

    @Column(name = "token", length = 256, nullable = false, unique = true)
    private String token;

    @Column(name = "date_sent", nullable = false)
    private LocalDateTime dateSent;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "subcategory", length = 100, nullable = false)
    private String subcategory;

    @PrePersist
    public void onCreate() {
        if (dateSent == null) {
            dateSent = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        dateSent = LocalDateTime.now();
    }
}
