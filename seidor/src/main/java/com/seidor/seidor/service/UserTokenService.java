package com.seidor.seidor.service;

import com.seidor.seidor.model.UserToken;
import com.seidor.seidor.pojo.TokenRequest;
import com.seidor.seidor.repository.UserTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserTokenService {

    private final UserTokenRepository repo;

    public UserTokenService(UserTokenRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public UserToken create(TokenRequest req) {
        UserToken t = new UserToken();

        String mailB64 = safeBase64(req.getMailBase64());
        t.setMailBase64(mailB64);
        t.setToken(UUID.randomUUID().toString());
        t.setCategory(req.getCategory());
        t.setSubcategory(req.getSubcategory());

        return repo.save(t);
    }

    @Transactional(readOnly = true)
    public List<UserToken> findByMailCategorySubcategory(String mailBase64OrPlain,
                                                         String category,
                                                         String subcategory) {
        String mailB64 = safeBase64(mailBase64OrPlain);
        return repo.findByMailBase64AndCategoryAndSubcategory(mailB64, category, subcategory);
    }

    @Transactional
    public RefreshResult refreshOrCreate(String mailBase64OrPlain,
                                         String category,
                                         String subcategory) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusHours(48);

        String mailB64 = safeBase64(mailBase64OrPlain);

        Optional<UserToken> opt = repo.findTopByMailBase64AndCategoryAndSubcategoryOrderByDateSentDesc(
                mailB64, category, subcategory
        );

        if (opt.isEmpty()) {
            UserToken t = new UserToken();
            t.setMailBase64(mailB64);
            t.setCategory(category);
            t.setSubcategory(subcategory);
            t.setToken(UUID.randomUUID().toString());
            UserToken saved = repo.save(t);
            return new RefreshResult(saved, true, true);
        }

        UserToken existing = opt.get();

        if (existing.getDateSent() == null || existing.getDateSent().isBefore(cutoff)) {
            existing.setToken(UUID.randomUUID().toString());
            UserToken saved = repo.save(existing);
            return new RefreshResult(saved, false, true);
        }

        return new RefreshResult(existing, false, false);
    }

    @Transactional(readOnly = true)
    public Optional<UserToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    @Transactional
    public boolean deleteByToken(String token) {
        return repo.deleteByToken(token) > 0;
    }

    @Transactional(readOnly = true)
    public List<UserToken> findByCategoryAndSubcategory(String category, String subcategory) {
        return repo.findByCategoryAndSubcategory(category, subcategory);
    }

    // --- helpers ---
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

    public static class RefreshResult {
        public final UserToken tokenRow;
        public final boolean created;
        public final boolean refreshed;

        public RefreshResult(UserToken tokenRow, boolean created, boolean refreshed) {
            this.tokenRow = tokenRow;
            this.created = created;
            this.refreshed = refreshed;
        }
    }
}
