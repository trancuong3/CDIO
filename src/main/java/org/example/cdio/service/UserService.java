package org.example.cdio.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.cdio.entity.Role;
import org.example.cdio.entity.Store;
import org.example.cdio.entity.User;
import org.example.cdio.repository.RoleRepository;
import org.example.cdio.repository.StoreRepository;
import org.example.cdio.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final StoreRepository storeRepo;
    private final PasswordEncoder passwordEncoder;

    public void createUser(
            String username,
            String password,
            String fullName,
            Long roleId,
            Long storeId
    ) {

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        Store store = null;
        if (storeId != null) {
            store = storeRepo.findById(storeId)
                    .orElseThrow(() -> new RuntimeException("Store không tồn tại"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setStore(store);
        user.setStatus(User.Status.ACTIVE);

        userRepo.save(user);
    }

    public void updateUser(
            Long id,
            String fullName,
            Long roleId,
            Long storeId
    ) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        Store store = null;
        if (storeId != null) {
            store = storeRepo.findById(storeId)
                    .orElseThrow(() -> new RuntimeException("Store không tồn tại"));
        }

        user.setFullName(fullName);
        user.setRole(role);
        user.setStore(store);

        userRepo.save(user);
    }

    public void lock(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setStatus(User.Status.LOCKED);
    }

    public void unlock(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        user.setStatus(User.Status.ACTIVE);
    }
    public void deleteUser(Long id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

      
        if (user.getRole().getId() == 2L) {
            throw new RuntimeException("Không được xóa ADMIN");
        }

        userRepo.delete(user);
    }
}