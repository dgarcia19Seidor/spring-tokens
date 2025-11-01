package com.seidor.seidor.controller;

import com.seidor.seidor.model.UserToken;
import com.seidor.seidor.pojo.TokenRequest;
import com.seidor.seidor.service.UserTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserTokenControllerTest {

    private UserTokenService service;
    private UserTokenController controller;

    @BeforeEach
    void setup() {
        service = mock(UserTokenService.class);
        controller = new UserTokenController(service);
    }

    @Test
    void create_returnsCreatedToken() {
        TokenRequest req = new TokenRequest();
        req.setMailBase64("test@test.com");
        req.setCategory("promo");
        req.setSubcategory("black-friday");

        UserToken saved = new UserToken();
        saved.setId(1L);
        saved.setToken("uuid-token-123");
        saved.setCategory("promo");
        saved.setSubcategory("black-friday");
        saved.setDateSent(LocalDateTime.of(2025, 11, 1, 12, 0));

        when(service.create(any(TokenRequest.class))).thenReturn(saved);

        ResponseEntity<Map<String, Object>> resp = controller.create(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        Map<String, Object> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id")).isEqualTo(1L);
        assertThat(body.get("category")).isEqualTo("promo");
        assertThat(body.get("subcategory")).isEqualTo("black-friday");
        assertThat(body.get("token")).isEqualTo("uuid-token-123");

        verify(service, times(1)).create(any(TokenRequest.class));
    }

    @Test
    void find_returnsListOfTokens_ifExists() {
        UserToken t = new UserToken();
        t.setId(2L);
        t.setToken("tok-123");
        t.setDateSent(LocalDateTime.of(2025, 11, 1, 12, 0));

        when(service.findByMailCategorySubcategory("test@test.com", "promo", "black-friday"))
                .thenReturn(List.of(t));

        ResponseEntity<List<Map<String,Object>>> resp = controller.find(
                "test@test.com", "promo", "black-friday"
        );

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        var body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).get("id")).isEqualTo(2L);
        assertThat(body.get(0).get("token")).isEqualTo("tok-123");
        assertThat(body.get(0).get("dateSent")).isEqualTo("2025-11-01T12:00");

        verify(service, times(1))
                .findByMailCategorySubcategory("test@test.com", "promo", "black-friday");
    }

    @Test
    void find_returns404_ifEmpty() {
        when(service.findByMailCategorySubcategory("test@test.com", "promo", "black-friday"))
                .thenReturn(List.of());

        ResponseEntity<List<Map<String,Object>>> resp = controller.find(
                "test@test.com", "promo", "black-friday"
        );

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody()).isEmpty();

        verify(service, times(1))
                .findByMailCategorySubcategory("test@test.com", "promo", "black-friday");
    }

    @Test
    void refreshOrCreate_returnsResponseWithCreatedOrUpdated() {
        TokenRequest req = new TokenRequest();
        req.setMailBase64("test@test.com");
        req.setCategory("promo");
        req.setSubcategory("black-friday");

        UserToken refreshed = new UserToken();
        refreshed.setId(50L);
        refreshed.setToken("fresh-token-uuid");
        refreshed.setCategory("promo");
        refreshed.setSubcategory("black-friday");
        refreshed.setDateSent(LocalDateTime.of(2025, 11, 1, 13, 0));

        UserTokenService.RefreshResult result = new UserTokenService.RefreshResult(
                refreshed,
                true,
                true
        );

        when(service.refreshOrCreate("test@test.com", "promo", "black-friday"))
                .thenReturn(result);

        ResponseEntity<Map<String,Object>> resp = controller.refreshOrCreate(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        var body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id")).isEqualTo(50L);
        assertThat(body.get("token")).isEqualTo("fresh-token-uuid");
        assertThat(body.get("category")).isEqualTo("promo");
        assertThat(body.get("subcategory")).isEqualTo("black-friday");
        assertThat(body.get("created")).isEqualTo(true);
        assertThat(body.get("refreshed")).isEqualTo(true);
        assertThat(body.get("dateSent")).isEqualTo(LocalDateTime.of(2025, 11, 1, 13, 0));

        verify(service, times(1))
                .refreshOrCreate("test@test.com", "promo", "black-friday");
    }

    @Test
    void getByToken_returnsOk_ifFound() {
        UserToken token = new UserToken();
        token.setId(10L);
        token.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        token.setToken("abc-123");
        token.setCategory("promo");
        token.setSubcategory("test");
        token.setDateSent(LocalDateTime.of(2025, 11, 1, 14, 30));

        when(service.findByToken("abc-123")).thenReturn(Optional.of(token));

        ResponseEntity<Map<String,Object>> resp = controller.getByToken("abc-123");

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        var body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id")).isEqualTo(10L);
        assertThat(body.get("token")).isEqualTo("abc-123");
        assertThat(body.get("mailBase64")).isEqualTo("dGVzdEB0ZXN0LmNvbQ==");
        assertThat(body.get("category")).isEqualTo("promo");
        assertThat(body.get("subcategory")).isEqualTo("test");
        Object dateSentObj = body.get("dateSent");
        assertThat(dateSentObj).isNotNull();
        assertThat(dateSentObj.toString())
                .isEqualTo("2025-11-01T14:30");


        verify(service, times(1)).findByToken("abc-123");
    }

    @Test
    void getByToken_returns404_ifNotFound() {
        when(service.findByToken("does-not-exist")).thenReturn(Optional.empty());

        ResponseEntity<Map<String,Object>> resp = controller.getByToken("does-not-exist");

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody()).isNull();

        verify(service, times(1)).findByToken("does-not-exist");
    }

    @Test
    void deleteByToken_returns204_ifDeleted() {
        when(service.deleteByToken("killme")).thenReturn(true);

        ResponseEntity<Void> resp = controller.deleteByToken("killme");

        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        verify(service, times(1)).deleteByToken("killme");
    }

    @Test
    void deleteByToken_returns404_ifNotDeleted() {
        when(service.deleteByToken("idontexist")).thenReturn(false);

        ResponseEntity<Void> resp = controller.deleteByToken("idontexist");

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(service, times(1)).deleteByToken("idontexist");
    }

    @Test
    void getMailsAndTokensByCategoryAndSubcategory_returnsList() {
        UserToken t = new UserToken();
        t.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        t.setToken("tok-999");

        when(service.findByCategoryAndSubcategory("promo", "black-friday"))
                .thenReturn(List.of(t));

        ResponseEntity<List<Map<String,String>>> resp =
                controller.getMailsAndTokensByCategoryAndSubcategory("promo", "black-friday");

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        var body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).get("mailBase64")).isEqualTo("dGVzdEB0ZXN0LmNvbQ==");
        assertThat(body.get(0).get("token")).isEqualTo("tok-999");

        verify(service, times(1))
                .findByCategoryAndSubcategory("promo", "black-friday");
    }

    @Test
    void getMailsAndTokensByCategoryAndSubcategory_returns404_ifEmpty() {
        when(service.findByCategoryAndSubcategory("promo", "black-friday"))
                .thenReturn(List.of());

        ResponseEntity<List<Map<String,String>>> resp =
                controller.getMailsAndTokensByCategoryAndSubcategory("promo", "black-friday");

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody()).isEmpty();

        verify(service, times(1))
                .findByCategoryAndSubcategory("promo", "black-friday");
    }
}
