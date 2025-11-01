package com.seidor.seidor.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTokenTest {

    @Test
    void settersAndGetters_shouldStoreValues() {
        UserToken t = new UserToken();

        t.setId(99L);
        t.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        t.setToken("abc-123-uuid");
        t.setCategory("promo");
        t.setSubcategory("black-friday");

        LocalDateTime sentTs = LocalDateTime.of(2025, 11, 1, 12, 0);
        t.setDateSent(sentTs);

        assertThat(t.getId()).isEqualTo(99L);
        assertThat(t.getMailBase64()).isEqualTo("dGVzdEB0ZXN0LmNvbQ==");
        assertThat(t.getToken()).isEqualTo("abc-123-uuid");
        assertThat(t.getCategory()).isEqualTo("promo");
        assertThat(t.getSubcategory()).isEqualTo("black-friday");
        assertThat(t.getDateSent()).isEqualTo(sentTs);
    }

    @Test
    void onCreate_shouldSetDateSent_ifNull() {
        UserToken t = new UserToken();

        assertThat(t.getDateSent()).isNull();

        t.onCreate();

        assertThat(t.getDateSent()).isNotNull();

        LocalDateTime createdTs = t.getDateSent();
        assertThat(createdTs)
                .isAfterOrEqualTo(LocalDateTime.now().minusSeconds(5));
    }

    @Test
    void onCreate_shouldNotOverrideExistingDateSent() {
        UserToken t = new UserToken();
        LocalDateTime preset = LocalDateTime.of(2025, 11, 1, 15, 0);
        t.setDateSent(preset);

        t.onCreate();

        assertThat(t.getDateSent()).isEqualTo(preset);
    }

    @Test
    void onUpdate_shouldRefreshDateSent() {
        UserToken t = new UserToken();

        LocalDateTime old = LocalDateTime.of(2025, 10, 31, 22, 0);
        t.setDateSent(old);

        t.onUpdate();

        assertThat(t.getDateSent()).isNotNull();
        assertThat(t.getDateSent())
                .isAfterOrEqualTo(LocalDateTime.now().minusSeconds(5));

        assertThat(t.getDateSent()).isNotEqualTo(old);
    }
}
