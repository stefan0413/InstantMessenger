package org.instantmessenger.backend.controller;

import jakarta.validation.Valid;
import org.instantmessenger.backend.dto.AuthResponse;
import org.instantmessenger.backend.dto.LoginRequest;
import org.instantmessenger.backend.dto.RegisterRequest;
import org.instantmessenger.backend.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(successPage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorPage(e.getMessage()));
        }
    }

    private String successPage() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <title>Email Verified</title>
                  <style>
                    body { font-family: Arial, sans-serif; display: flex; justify-content: center;
                           align-items: center; height: 100vh; margin: 0; background: #f9fafb; }
                    .card { background: white; padding: 40px; border-radius: 12px;
                            box-shadow: 0 4px 12px rgba(0,0,0,.1); text-align: center; max-width: 400px; }
                    h1 { color: #16a34a; }
                    p { color: #6b7280; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>&#10003; Email Verified</h1>
                    <p>Your email address has been successfully verified.</p>
                    <p>You can now close this window and log in to InstantMessenger.</p>
                  </div>
                </body>
                </html>
                """;
    }

    private String errorPage(String message) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <title>Verification Failed</title>
                  <style>
                    body { font-family: Arial, sans-serif; display: flex; justify-content: center;
                           align-items: center; height: 100vh; margin: 0; background: #f9fafb; }
                    .card { background: white; padding: 40px; border-radius: 12px;
                            box-shadow: 0 4px 12px rgba(0,0,0,.1); text-align: center; max-width: 400px; }
                    h1 { color: #dc2626; }
                    p { color: #6b7280; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>&#10007; Verification Failed</h1>
                    <p>%s</p>
                    <p>Please try registering again or contact support.</p>
                  </div>
                </body>
                </html>
                """.formatted(message);
    }
}
