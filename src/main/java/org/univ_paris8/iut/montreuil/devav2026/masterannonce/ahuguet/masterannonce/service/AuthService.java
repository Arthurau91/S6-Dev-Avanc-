package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.LoginRequestDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.LoginResponseDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.BusinessException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.JwtTokenProvider;

/**
 * Service handling user authentication via JWT.
 */
@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticate user and return JWT token.
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("UNAUTHORIZED", "Identifiants invalides", 401));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("UNAUTHORIZED", "Identifiants invalides", 401);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        long expiresInSeconds = jwtTokenProvider.getExpirationMs() / 1000;

        LOG.info("User '{}' logged in successfully", request.getUsername());
        return new LoginResponseDTO(token, expiresInSeconds);
    }
}
