package com.seidor.seidor.service;

import com.seidor.seidor.model.UserToken;
import com.seidor.seidor.pojo.TokenRequest;
import com.seidor.seidor.repository.UserTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserTokenServiceTest {

    private UserTokenRepository repo;
    private UserTokenService service;

    @BeforeEach
    void setup() {
        repo = mock(UserTokenRepository.class);
        service = new UserTokenService(repo);
    }

    @Test
    void create_generatesToken_andSaves_withPlainEmailEncoded() {
        TokenRequest req = new TokenRequest();
        req.setMailBase64("test@test.com");
        req.setCategory("user-validation");
        req.setSubcategory("register");

        when(repo.save(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken passed = invocation.getArgument(0);
            passed.setId(123L);
            passed.setDateSent(LocalDateTime.now());
            return passed;
        });

        UserToken saved = service.create(req);

        assertThat(saved.getId()).isEqualTo(123L);
        assertThat(saved.getCategory()).isEqualTo("user-validation");
        assertThat(saved.getSubcategory()).isEqualTo("register");

        assertThat(saved.getToken()).isNotNull();

        String expectedB64 = Base64.getEncoder().encodeToString("test@test.com".getBytes());
        assertThat(saved.getMailBase64()).isEqualTo(expectedB64);

        verify(repo, times(1)).save(any(UserToken.class));
    }

    @Test
    void create_allowsNullMail_andStillSaves() {
        TokenRequest req = new TokenRequest();
        req.setMailBase64(null);
        req.setCategory("validation");
        req.setSubcategory("signup");

        when(repo.save(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken t = invocation.getArgument(0);
            t.setId(11L);
            t.setDateSent(LocalDateTime.now());
            return t;
        });

        UserToken saved = service.create(req);

        assertThat(saved.getId()).isEqualTo(11L);
        assertThat(saved.getMailBase64()).isNull();
        assertThat(saved.getCategory()).isEqualTo("validation");
        assertThat(saved.getSubcategory()).isEqualTo("signup");
        assertThat(saved.getToken()).isNotNull();

        verify(repo, times(1)).save(any(UserToken.class));
    }

    @Test
    void findByMailCategorySubcategory_encodesPlainEmailToBase64() {
        when(repo.findByMailBase64AndCategoryAndSubcategory(anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        var result = service.findByMailCategorySubcategory("test@test.com", "promo", "bf");

        assertThat(result).isEmpty();

        ArgumentCaptor<String> mailCaptor = ArgumentCaptor.forClass(String.class);

        verify(repo).findByMailBase64AndCategoryAndSubcategory(
                mailCaptor.capture(),
                eq("promo"),
                eq("bf")
        );

        String expectedB64 = Base64.getEncoder().encodeToString("test@test.com".getBytes());
        assertThat(mailCaptor.getValue()).isEqualTo(expectedB64);
    }

    @Test
    void findByMailCategorySubcategory_ifAlreadyB64_usesItDirectly() {
        String alreadyB64 = "dGVzdEB0ZXN0LmNvbQ==";

        when(repo.findByMailBase64AndCategoryAndSubcategory(anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        var result = service.findByMailCategorySubcategory(alreadyB64, "x", "y");
        assertThat(result).isEmpty();

        ArgumentCaptor<String> mailCaptor = ArgumentCaptor.forClass(String.class);

        verify(repo).findByMailBase64AndCategoryAndSubcategory(
                mailCaptor.capture(),
                eq("x"),
                eq("y")
        );

        assertThat(mailCaptor.getValue()).isEqualTo(alreadyB64);
    }

    @Test
    void refreshOrCreate_noExistingToken_createsOneWithUuid() {
        String mail = "test@test.com";
        String category = "promo";
        String subcat = "bf";

        when(repo.findTopByMailBase64AndCategoryAndSubcategoryOrderByDateSentDesc(
                anyString(), anyString(), anyString()
        )).thenReturn(Optional.empty());

        when(repo.save(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken tok = invocation.getArgument(0);
            tok.setId(321L);
            tok.setDateSent(LocalDateTime.now());
            return tok;
        });

        UserTokenService.RefreshResult result = service.refreshOrCreate(mail, category, subcat);

        assertThat(result.created).isTrue();
        assertThat(result.refreshed).isTrue();
        assertThat(result.tokenRow.getId()).isEqualTo(321L);
        assertThat(result.tokenRow.getCategory()).isEqualTo("promo");
        assertThat(result.tokenRow.getSubcategory()).isEqualTo("bf");
        assertThat(result.tokenRow.getToken()).isNotNull();
    }

    @Test
    void refreshOrCreate_existingRecentToken_keepsIt() {
        String category = "promo";
        String subcat = "bf";

        UserToken fresh = new UserToken();
        fresh.setId(1000L);
        fresh.setCategory(category);
        fresh.setSubcategory(subcat);
        fresh.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        fresh.setToken("same-token");
        fresh.setDateSent(LocalDateTime.now().minusHours(1));

        when(repo.findTopByMailBase64AndCategoryAndSubcategoryOrderByDateSentDesc(
                anyString(), anyString(), anyString()
        )).thenReturn(Optional.of(fresh));

        UserTokenService.RefreshResult result =
                service.refreshOrCreate("test@test.com", category, subcat);

        assertThat(result.created).isFalse();
        assertThat(result.refreshed).isFalse();
        assertThat(result.tokenRow.getId()).isEqualTo(1000L);
        assertThat(result.tokenRow.getToken()).isEqualTo("same-token");

        verify(repo, never()).save(any(UserToken.class));
    }

    @Test
    void refreshOrCreate_existingOldToken_refreshesUuid() {
        String category = "promo";
        String subcat = "bf";

        UserToken old = new UserToken();
        old.setId(2000L);
        old.setCategory(category);
        old.setSubcategory(subcat);
        old.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        old.setToken("old-token-123");
        old.setDateSent(LocalDateTime.now().minusHours(72));

        when(repo.findTopByMailBase64AndCategoryAndSubcategoryOrderByDateSentDesc(
                anyString(), anyString(), anyString()
        )).thenReturn(Optional.of(old));

        when(repo.save(any(UserToken.class))).thenAnswer(invocation -> {
            UserToken updated = invocation.getArgument(0);
            updated.setDateSent(LocalDateTime.now());
            return updated;
        });

        UserTokenService.RefreshResult result =
                service.refreshOrCreate("test@test.com", category, subcat);

        assertThat(result.created).isFalse();
        assertThat(result.refreshed).isTrue();
        assertThat(result.tokenRow.getId()).isEqualTo(2000L);
        assertThat(result.tokenRow.getToken()).isNotEqualTo("old-token-123");
    }

    @Test
    void findByToken_delegatesToRepo() {
        UserToken t = new UserToken();
        t.setId(123L);
        t.setToken("abc-123");
        t.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        t.setCategory("c1");
        t.setSubcategory("s1");
        t.setDateSent(LocalDateTime.of(2025, 11, 1, 10, 0));

        when(repo.findByToken("abc-123")).thenReturn(Optional.of(t));

        Optional<UserToken> res = service.findByToken("abc-123");

        assertThat(res).isPresent();
        assertThat(res.get().getId()).isEqualTo(123L);

        verify(repo, times(1)).findByToken("abc-123");
    }

    @Test
    void deleteByToken_returnsTrueIfRowDeleted() {
        when(repo.deleteByToken("kill-me")).thenReturn(1L);

        boolean deleted = service.deleteByToken("kill-me");

        assertThat(deleted).isTrue();
        verify(repo, times(1)).deleteByToken("kill-me");
    }

    @Test
    void deleteByToken_returnsFalseIfNoRowDeleted() {
        when(repo.deleteByToken("nope")).thenReturn(0L);

        boolean deleted = service.deleteByToken("nope");

        assertThat(deleted).isFalse();
        verify(repo, times(1)).deleteByToken("nope");
    }

    @Test
    void findByCategoryAndSubcategory_delegatesToRepo() {
        UserToken t = new UserToken();
        t.setId(9L);

        when(repo.findByCategoryAndSubcategory("promo", "black-friday"))
                .thenReturn(List.of(t));

        List<UserToken> res =
                service.findByCategoryAndSubcategory("promo", "black-friday");

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getId()).isEqualTo(9L);

        verify(repo, times(1))
                .findByCategoryAndSubcategory("promo", "black-friday");
    }
}
