package controller;

import controller.dto.ApiError;
import controller.dto.AuthResponse;
import controller.dto.LoginRequest;
import controller.dto.RegisterRequest;
import core.user.User;
import core.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import security.CustomUserDetails;
import security.JwtTokenProvider;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final Integer DEFAULT_USER_ROLE_ID = 2; // USER role

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ApiError(409, "Email already registered"));
        }

        // Create new user with hashed password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User(
                null,
                DEFAULT_USER_ROLE_ID,
                request.getUsername(),
                request.getEmail(),
                hashedPassword
        );
        userRepository.save(newUser);

        // Retrieve saved user to get ID
        User savedUser = userRepository.findUserByMail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User registration failed"));

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                savedUser.getId().orElse(null),
                savedUser.getEmail()
        );

        AuthResponse response = new AuthResponse(
                token,
                savedUser.getId().orElse(null),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoleName() != null ? savedUser.getRoleName() : "USER"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(authentication);

            AuthResponse response = new AuthResponse(
                    token,
                    userDetails.getId(),
                    userDetails.getDisplayName(),
                    userDetails.getEmail(),
                    userDetails.getRole()
            );

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiError(401, "Invalid email or password"));
        }
    }
}
