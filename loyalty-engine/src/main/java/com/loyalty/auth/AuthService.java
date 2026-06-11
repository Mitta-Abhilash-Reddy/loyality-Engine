package com.loyalty.auth;

import com.loyalty.customer.Customer;
import com.loyalty.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // tier, points, churnScore defaults are set in the entity

        Customer saved = customerRepository.save(customer);
        String token = jwtUtil.generateToken(saved.getEmail());

        return new AuthResponse(token, saved.getEmail(), saved.getName(),
                saved.getTier().name(), saved.getId());
    }

    public AuthResponse login(AuthRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(customer.getEmail());
        return new AuthResponse(token, customer.getEmail(), customer.getName(),
                customer.getTier().name(), customer.getId());
    }
}
