package com.seidor.seidor.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void settersAndGetters_shouldStoreValues() {
        User u = new User();

        u.setId(5L);
        u.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        u.setCategory("promos");
        u.setSubcategory("black-friday");
        LocalDateTime ts = LocalDateTime.of(2025, 11, 1, 10, 0);
        u.setDateSubscribed(ts);

        assertThat(u.getId()).isEqualTo(5L);
        assertThat(u.getMailBase64()).isEqualTo("dGVzdEB0ZXN0LmNvbQ==");
        assertThat(u.getCategory()).isEqualTo("promos");
        assertThat(u.getSubcategory()).isEqualTo("black-friday");
        assertThat(u.getDateSubscribed()).isEqualTo(ts);
    }

    @Test
    void onCreate_shouldSetDateSubscribed_ifNull() {
        User u = new User();

        assertThat(u.getDateSubscribed()).isNull();

        u.onCreate();

        assertThat(u.getDateSubscribed()).isNotNull();

        LocalDateTime afterCall = u.getDateSubscribed();
        assertThat(afterCall)
                .isAfterOrEqualTo(LocalDateTime.now().minusSeconds(5));
    }

    @Test
    void onCreate_shouldNotOverrideExistingDateSubscribed() {
        User u = new User();

        LocalDateTime preset = LocalDateTime.of(2025, 11, 1, 18, 30);
        u.setDateSubscribed(preset);

        u.onCreate();

        assertThat(u.getDateSubscribed()).isEqualTo(preset);
    }
}
