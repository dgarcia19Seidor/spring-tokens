package com.seidor.seidor.service;

import com.seidor.seidor.model.User;
import com.seidor.seidor.pojo.UserRequest;
import com.seidor.seidor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setup() {
        repo = mock(UserRepository.class);
        service = new UserService(repo);
    }

    @Test
    void subscribe_insertsNewRow_ifNotExisting_andEncodesPlainEmailToBase64() {
        UserRequest req = new UserRequest();
        req.setMailBase64("test@test.com");
        req.setCategory("promos");
        req.setSubcategory("black-friday");

        when(repo.findByMailBase64AndCategoryAndSubcategory(anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        when(repo.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            u.setDateSubscribed(LocalDateTime.now());
            return u;
        });

        User result = service.subscribe(req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCategory()).isEqualTo("promos");
        assertThat(result.getSubcategory()).isEqualTo("black-friday");

        String expectedB64 = Base64.getEncoder()
                .encodeToString("test@test.com".getBytes());
        assertThat(result.getMailBase64()).isEqualTo(expectedB64);

        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void subscribe_usesIncomingBase64_ifAlreadyLooksBase64() {
        String alreadyB64 = "dGVzdEB0ZXN0LmNvbQ==";

        UserRequest req = new UserRequest();
        req.setMailBase64(alreadyB64);
        req.setCategory("alerts");
        req.setSubcategory("security");

        when(repo.findByMailBase64AndCategoryAndSubcategory(anyString(), anyString(), anyString()))
                .thenReturn(List.of());

        when(repo.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(777L);
            u.setDateSubscribed(LocalDateTime.now());
            return u;
        });

        User result = service.subscribe(req);

        assertThat(result.getId()).isEqualTo(777L);
        assertThat(result.getMailBase64()).isEqualTo(alreadyB64);
        assertThat(result.getCategory()).isEqualTo("alerts");
        assertThat(result.getSubcategory()).isEqualTo("security");

        ArgumentCaptor<String> mailCaptured = ArgumentCaptor.forClass(String.class);

        verify(repo, times(1))
                .findByMailBase64AndCategoryAndSubcategory(
                        mailCaptured.capture(),
                        eq("alerts"),
                        eq("security")
                );

        assertThat(mailCaptured.getValue()).isEqualTo(alreadyB64);
        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void subscribe_handlesNullMailWithoutExploding_andSavesNull() {
        UserRequest req = new UserRequest();
        req.setMailBase64(null);
        req.setCategory("promo");
        req.setSubcategory("vip");

        when(repo.findByMailBase64AndCategoryAndSubcategory(any(), anyString(), anyString()))
                .thenReturn(List.of());

        when(repo.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(55L);
            u.setDateSubscribed(LocalDateTime.now());
            return u;
        });

        User result = service.subscribe(req);

        assertThat(result.getId()).isEqualTo(55L);
        assertThat(result.getMailBase64()).isNull();
        assertThat(result.getCategory()).isEqualTo("promo");
        assertThat(result.getSubcategory()).isEqualTo("vip");

        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void subscribe_returnsExisting_ifAlreadyExists() {
        UserRequest req = new UserRequest();
        req.setMailBase64("test@test.com");
        req.setCategory("promos");
        req.setSubcategory("black-friday");

        User existing = new User();
        existing.setId(99L);
        existing.setMailBase64("dGVzdEB0ZXN0LmNvbQ==");
        existing.setCategory("promos");
        existing.setSubcategory("black-friday");
        existing.setDateSubscribed(LocalDateTime.now().minusDays(1));

        when(repo.findByMailBase64AndCategoryAndSubcategory(anyString(), anyString(), anyString()))
                .thenReturn(List.of(existing));

        User result = service.subscribe(req);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getCategory()).isEqualTo("promos");
        assertThat(result.getSubcategory()).isEqualTo("black-friday");

        verify(repo, never()).save(any(User.class));
    }

    @Test
    void findAll_delegatesToRepo() {
        User u1 = new User();
        u1.setId(1L);
        User u2 = new User();
        u2.setId(2L);

        when(repo.findAll()).thenReturn(List.of(u1, u2));

        List<User> all = service.findAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isEqualTo(1L);
        assertThat(all.get(1).getId()).isEqualTo(2L);

        verify(repo, times(1)).findAll();
    }

    @Test
    void findByCategoryAndSubcategory_delegatesToRepo() {
        User u1 = new User();
        u1.setId(1L);
        User u2 = new User();
        u2.setId(2L);

        when(repo.findByCategoryAndSubcategory("promos", "black-friday"))
                .thenReturn(List.of(u1, u2));

        List<User> list = service.findByCategoryAndSubcategory("promos", "black-friday");

        assertThat(list).hasSize(2);
        verify(repo, times(1))
                .findByCategoryAndSubcategory("promos", "black-friday");
    }

    @Test
    void deleteById_returnsTrueIfDeleted() {
        User u = new User();
        u.setId(42L);

        when(repo.findById(42L)).thenReturn(Optional.of(u));

        boolean deleted = service.deleteById(42L);

        assertThat(deleted).isTrue();
        verify(repo, times(1)).deleteById(42L);
    }

    @Test
    void deleteById_returnsFalseIfNotExists() {
        when(repo.findById(123L)).thenReturn(Optional.empty());

        boolean deleted = service.deleteById(123L);

        assertThat(deleted).isFalse();
        verify(repo, never()).deleteById(anyLong());
    }
}
