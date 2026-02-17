package org.example.cdio.service;

import lombok.RequiredArgsConstructor;
import org.example.cdio.dto.StoreRegisterRequest;
import org.example.cdio.entity.Role;
import org.example.cdio.entity.Store;
import org.example.cdio.entity.User;
import org.example.cdio.repository.RoleRepository;
import org.example.cdio.repository.StoreRepository;
import org.example.cdio.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerStore(StoreRegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        Role storeRole = roleRepository.findByName("STORE")
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy role STORE trong DB"));

        Store store = new Store();
        store.setName(req.getStoreName());
        store.setAddress(req.getAddress());
        store.setRepresentativeName(req.getRepresentativeName());
        store.setPhone(req.getPhone());
        store.setStatus(Store.Status.ACTIVE);
        Store savedStore = storeRepository.save(store);

        User user = new User();
        user.setRole(storeRole);
        user.setStore(savedStore);
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword())); // BCrypt
        user.setFullName(req.getFullName());
        user.setStatus(User.Status.ACTIVE);

        userRepository.save(user);
    }
}
