package com.seidor.seidor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seidor.seidor.model.User;
import com.seidor.seidor.pojo.UserRequest;
import com.seidor.seidor.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService service;
    private UserController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        service = mock(UserService.class);
        controller = new UserController(service);
        objectMapper = new ObjectMapper();
    }

    @Test
    void subscribe_returnsCreatedUser() throws Exception {
        UserRequest req = new UserRequest();
        req.setMailBase64("test@test.com");
        req.setCategory("promos");
        req.setSubcategory("black-friday");

        User saved = new User();
        saved.setId(12L);
        saved.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        saved.setCategory("promos");
        saved.setSubcategory("black-friday");
        saved.setDateSubscribed(LocalDateTime.of(2025, 11, 1, 18, 45));

        when(service.subscribe(any(UserRequest.class))).thenReturn(saved);

        ResponseEntity<java.util.Map<String, Object>> response = controller.subscribe(req);

        assertThat(response.getStatusCode().value()).isEqualTo(201);

        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("id")).isEqualTo(12L);
        assertThat(body.get("mailBase64")).isEqualTo("dGVzdEB0ZXN0LmNvbQ==");
        assertThat(body.get("category")).isEqualTo("promos");
        assertThat(body.get("subcategory")).isEqualTo("black-friday");
        assertThat(body.get("dateSubscribed"))
                .isEqualTo(LocalDateTime.of(2025, 11, 1, 18, 45));


        verify(service, times(1)).subscribe(any(UserRequest.class));
    }

    @Test
    void getAll_returnsAllSubscriptions() {
        User u1 = new User();
        u1.setId(1L);
        u1.setMailBase64("bWFpbDE=");
        u1.setCategory("promos");
        u1.setSubcategory("black-friday");
        u1.setDateSubscribed(LocalDateTime.of(2025, 11, 1, 10, 0));

        User u2 = new User();
        u2.setId(2L);
        u2.setMailBase64("bWFpbDI=");
        u2.setCategory("news");
        u2.setSubcategory("daily");
        u2.setDateSubscribed(LocalDateTime.of(2025, 11, 1, 11, 0));

        when(service.findAll()).thenReturn(List.of(u1, u2));

        ResponseEntity<List<java.util.Map<String, Object>>> resp = controller.getAll();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        var list = resp.getBody();
        assertThat(list).isNotNull();
        assertThat(list).hasSize(2);
        assertThat(list.get(0).get("id")).isEqualTo(1L);
        assertThat(list.get(1).get("id")).isEqualTo(2L);

        verify(service, times(1)).findAll();
    }

    @Test
    void getAll_returns204_ifEmpty() {
        when(service.findAll()).thenReturn(List.of());

        ResponseEntity<List<java.util.Map<String, Object>>> resp = controller.getAll();

        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        assertThat(resp.getBody()).isEmpty();

        verify(service, times(1)).findAll();
    }

    @Test
    void getMailsForSegment_returnsList() {
        User u1 = new User();
        u1.setMailBase64("bWFpbDE=");
        User u2 = new User();
        u2.setMailBase64("bWFpbDI=");

        when(service.findByCategoryAndSubcategory("promos", "black-friday"))
                .thenReturn(List.of(u1, u2));

        ResponseEntity<List<String>> resp =
                controller.getMailsForSegment("promos", "black-friday");

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).containsExactly("bWFpbDE=", "bWFpbDI=");

        verify(service, times(1))
                .findByCategoryAndSubcategory("promos", "black-friday");
    }

    @Test
    void getMailsForSegment_returns204_ifEmpty() {
        when(service.findByCategoryAndSubcategory("promos", "black-friday"))
                .thenReturn(List.of());

        ResponseEntity<List<String>> resp =
                controller.getMailsForSegment("promos", "black-friday");

        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        assertThat(resp.getBody()).isEmpty();

        verify(service, times(1))
                .findByCategoryAndSubcategory("promos", "black-friday");
    }

    @Test
    void deleteSubscription_returns204_ifDeleted() {
        when(service.deleteById(42L)).thenReturn(true);

        ResponseEntity<Void> resp = controller.deleteSubscription(42L);

        assertThat(resp.getStatusCode().value()).isEqualTo(204);
        verify(service, times(1)).deleteById(42L);
    }

    @Test
    void deleteSubscription_returns404_ifNotFound() {
        when(service.deleteById(99L)).thenReturn(false);

        ResponseEntity<Void> resp = controller.deleteSubscription(99L);

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(service, times(1)).deleteById(99L);
    }
}
