package com.seidor.seidor.repository;

import com.seidor.seidor.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByToken(String token);

    List<UserToken> findByMailBase64AndCategoryAndSubcategory(
            String mailBase64, String category, String subcategory
    );

    Optional<UserToken> findTopByMailBase64AndCategoryAndSubcategoryOrderByDateSentDesc(
            String mailBase64, String category, String subcategory
    );

    long deleteByToken(String token);

    List<UserToken> findByCategoryAndSubcategory(String category, String subcategory);
}
