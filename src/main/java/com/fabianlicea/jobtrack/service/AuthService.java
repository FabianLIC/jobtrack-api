package com.fabianlicea.jobtrack.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fabianlicea.jobtrack.dto.AuthResponse;
import com.fabianlicea.jobtrack.dto.LoginRequest;
import com.fabianlicea.jobtrack.dto.RegisterRequest;
import com.fabianlicea.jobtrack.exceptions.EmailAlreadyExistsException;
import com.fabianlicea.jobtrack.model.User;
import com.fabianlicea.jobtrack.repository.UserRepository;
import com.fabianlicea.jobtrack.security.JwtService;
import com.fabianlicea.jobtrack.security.UserPrincipal;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EmailAlreadyExistsException(registerRequest.email());
        }

        User user = new User(registerRequest.name(), registerRequest.email(),
                passwordEncoder.encode(registerRequest.password()));
        userRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, "Bearer", registerRequest.email(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UsernameNotFoundException(request.email()));

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, "Bearer", request.email(), user.getName());

    }

}
