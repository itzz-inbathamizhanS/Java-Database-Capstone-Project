package com.project.back_end.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // 🔐 Generate signing key
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ✅ BASIC TOKEN (only email)
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(getSigningKey())
                .compact();
    }

    // ✅ ADVANCED TOKEN (role + id)
    public String generateToken(Object user, String role, String email) {

        Long id = null;

        if (user instanceof Doctor) {
            id = ((Doctor) user).getId();
        } else if (user instanceof Patient) {
            id = ((Patient) user).getId();
        } else if (user instanceof Admin) {
            id = ((Admin) user).getId();
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("id", id);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(getSigningKey())
                .compact();
    }

    // ✅ Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ✅ Extract email
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ Required method (now implemented)
    public String extractEmailFromToken(String token) {
        return extractEmail(token);
    }

    // ✅ Extract role
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ✅ Extract user ID
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("id", Long.class);
    }

    // ✅ Required method (doctor ID)
    public Long extractDoctorIdFromToken(String token) {
        return extractUserId(token);
    }

    // ✅ Token validation
    public boolean validateToken(String token, String user) {
        try {
            String extractedEmail = extractEmail(token);
            String role = extractRole(token);

            if (!role.equals(user)) {
                return false;
            }

            // Optional DB validation (extra safety)
            if (user.equals("admin")) {
                Admin admin = adminRepository.findByUsername(extractedEmail);
                return admin != null;
            } 
            else if (user.equals("doctor")) {
                Doctor doctor = doctorRepository.findByEmail(extractedEmail);
                return doctor != null;
            } 
            else if (user.equals("patient")) {
                Patient patient = patientRepository.findByEmail(extractedEmail);
                return patient != null;
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }
}
