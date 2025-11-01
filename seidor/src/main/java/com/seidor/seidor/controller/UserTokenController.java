package com.seidor.seidor.controller;

import com.seidor.seidor.model.UserToken;
import com.seidor.seidor.pojo.TokenRequest;
import com.seidor.seidor.service.UserTokenService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tokens")
public class UserTokenController {

    private final UserTokenService service;
    private static final Logger log = LoggerFactory.getLogger(UserTokenController.class);

    public UserTokenController(UserTokenService service) {
        this.service = service;
    }

    // POST /api/tokens
    @Operation(summary = "Create token row for pending email validation")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody TokenRequest req) {

        log.info("⟶ POST /api/tokens (create)");
        UserToken saved = service.create(req);

        Map<String, Object> body = new HashMap<>();
        body.put("id", saved.getId());
        body.put("token", saved.getToken());
        body.put("category", saved.getCategory());
        body.put("subcategory", saved.getSubcategory());

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // GET /api/tokens?mail=...&category=...&subcategory=...
    @Operation(summary = "Find tokens by mail, category and subcategory")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> find(
            @RequestParam("mail") String mailBase64OrPlain,
            @RequestParam("category") String category,
            @RequestParam("subcategory") String subcategory
    ) {
        log.info("⟶ GET /api/tokens params mail={}, category={}, sub={}", mailBase64OrPlain, category, subcategory);

        var tokens = service.findByMailCategorySubcategory(mailBase64OrPlain, category, subcategory);

        if (tokens.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<Map<String, Object>> response = tokens.stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", t.getId());
                    m.put("token", t.getToken());
                    m.put("dateSent", t.getDateSent().toString());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // POST /api/tokens/refresh
    @Operation(summary = "Refresh or create token if older than 48h")
    @PostMapping(
            path = "/refresh",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> refreshOrCreate(@Valid @RequestBody TokenRequest req) {

        log.info("⟶ POST /api/tokens/refresh for mail={}, category={}, sub={}",
                req.getMailBase64(), req.getCategory(), req.getSubcategory());

        var result = service.refreshOrCreate(
                req.getMailBase64(),
                req.getCategory(),
                req.getSubcategory()
        );

        var t = result.tokenRow;

        Map<String, Object> body = new HashMap<>();
        body.put("id", t.getId());
        body.put("token", t.getToken());
        body.put("category", t.getCategory());
        body.put("subcategory", t.getSubcategory());
        body.put("dateSent", t.getDateSent());
        body.put("created", result.created);
        body.put("refreshed", result.refreshed);

        var status = result.created ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(body);
    }

    // GET /api/tokens/{token}
    @Operation(summary = "Get token information by token value")
    @GetMapping(path = "/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getByToken(@PathVariable("token") String token) {

        log.info("⟶ GET /api/tokens/{}", token);

        return service.findByToken(token)
                .map(t -> {
                    Map<String, Object> body = new HashMap<>();
                    body.put("id", t.getId());
                    body.put("mailBase64", t.getMailBase64());
                    body.put("token", t.getToken());
                    body.put("category", t.getCategory());
                    body.put("subcategory", t.getSubcategory());
                    body.put("dateSent", t.getDateSent().toString());
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // DELETE /api/tokens/{token}
    @Operation(summary = "Delete token by token value")
    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deleteByToken(@PathVariable("token") String token) {
        log.info("⟶ DELETE /api/tokens/{}", token);

        boolean deleted = service.deleteByToken(token);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // GET /api/tokens/mails?category=...&subcategory=...
    @Operation(summary = "Get mails and tokens by category/subcategory")
    @GetMapping(path = "/mails", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, String>>> getMailsAndTokensByCategoryAndSubcategory(
            @RequestParam String category,
            @RequestParam String subcategory
    ) {
        log.info("⟶ GET /api/tokens/mails category={}, sub={}", category, subcategory);

        var tokens = service.findByCategoryAndSubcategory(category, subcategory);

        if (tokens.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<Map<String, String>> result = tokens.stream()
                .map(t -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("mailBase64", t.getMailBase64());
                    m.put("token", t.getToken());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
