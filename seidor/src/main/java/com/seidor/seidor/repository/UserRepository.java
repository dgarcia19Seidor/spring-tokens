package com.seidor.seidor.repository;

import com.seidor.seidor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByCategoryAndSubcategory(String category, String subcategory);

    List<User> findByMailBase64AndCategoryAndSubcategory(
            String mailBase64,
            String category,
            String subcategory
    );
}
