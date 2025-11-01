package com.seidor.seidor.controller;

import com.seidor.seidor.model.User;
import com.seidor.seidor.pojo.UserRequest;
import com.seidor.seidor.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // POST /api/users
    @Operation(summary = "Subscribe a user (mail) to a category/subcategory")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> subscribe(
            @Valid @RequestBody UserRequest req) {

        log.info("⟶ POST /api/users mail={}, cat={}, sub={}",
                req.getMailBase64(), req.getCategory(), req.getSubcategory());

        User saved = service.subscribe(req);

        Map<String, Object> body = new HashMap<>();
        body.put("id", saved.getId());
        body.put("mailBase64", saved.getMailBase64());
        body.put("category", saved.getCategory());
        body.put("subcategory", saved.getSubcategory());
        body.put("dateSubscribed", saved.getDateSubscribed());

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // GET /api/users
    @Operation(summary = "Get entire subscriptions table")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getAll() {

        log.info("⟶ GET /api/users");

        List<User> subs = service.findAll();

        if (subs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
        }

        List<Map<String, Object>> response = subs.stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getId());
                    m.put("mailBase64", s.getMailBase64());
                    m.put("category", s.getCategory());
                    m.put("subcategory", s.getSubcategory());
                    m.put("dateSubscribed", s.getDateSubscribed());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // GET /api/users/mails?category=...&subcategory=...
    @Operation(summary = "Get subscribed mails (base64) for a category/subcategory")
    @GetMapping(path = "/mails", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getMailsForSegment(
            @RequestParam String category,
            @RequestParam String subcategory
    ) {
        log.info("⟶ GET /api/users/mails cat={}, sub={}", category, subcategory);

        List<User> subs = service.findByCategoryAndSubcategory(category, subcategory);

        if (subs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
        }

        List<String> mailsBase64 = subs.stream()
                .map(User::getMailBase64)
                .collect(Collectors.toList());

        return ResponseEntity.ok(mailsBase64);
    }

    // DELETE /api/users/{id}
    @Operation(summary = "Delete a subscription row by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {

        log.info("⟶ DELETE /api/users/{}", id);

        boolean deleted = service.deleteById(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
