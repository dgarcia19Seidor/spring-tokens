package com.seidor.seidor.service;

import com.seidor.seidor.model.User;
import com.seidor.seidor.pojo.UserRequest;
import com.seidor.seidor.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public User subscribe(UserRequest req) {
        String mailB64 = safeBase64(req.getMailBase64());

        List<User> existing =
                repo.findByMailBase64AndCategoryAndSubcategory(
                        mailB64,
                        req.getCategory(),
                        req.getSubcategory()
                );

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        User u = new User();
        u.setMailBase64(mailB64);
        u.setCategory(req.getCategory());
        u.setSubcategory(req.getSubcategory());

        return repo.save(u);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByCategoryAndSubcategory(String category, String subcategory) {
        return repo.findByCategoryAndSubcategory(category, subcategory);
    }

    @Transactional
    public boolean deleteById(Long id) {
        Optional<User> row = repo.findById(id);
        if (row.isPresent()) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    // ---------- helpers ----------

    private static String safeBase64(String mailBase64OrPlain) {
        if (mailBase64OrPlain == null) return null;
        String s = mailBase64OrPlain.trim();
        if (looksLikeBase64(s)) return s;
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean looksLikeBase64(String s) {
        if (s.contains("@")) return false;
        if (s.length() < 16) return false;
        return s.matches("^[A-Za-z0-9+/_=\\-]+$");
    }
}
