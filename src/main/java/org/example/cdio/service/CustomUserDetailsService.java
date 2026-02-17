package org.example.cdio.service;

import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.User;
import org.example.cdio.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user"));

        if (u.getStatus() == User.Status.LOCKED) {
            throw new DisabledException("Tài khoản đã bị khóa");
        }

        String role = "ROLE_" + u.getRole().getName(); // ROLE_ADMIN / ROLE_STORE
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
