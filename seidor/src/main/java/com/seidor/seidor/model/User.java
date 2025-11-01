package com.seidor.seidor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "USER_SUBSCRIPTION"
)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mail_base64", length = 512, nullable = false)
    private String mailBase64;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "subcategory", length = 100, nullable = false)
    private String subcategory;

    @Column(name = "date_subscribed", nullable = false)
    private LocalDateTime dateSubscribed;

    @PrePersist
    public void onCreate() {
        if (dateSubscribed == null) {
            dateSubscribed = LocalDateTime.now();
        }
    }
}
