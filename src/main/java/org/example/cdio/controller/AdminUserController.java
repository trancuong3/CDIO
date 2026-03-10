package org.example.cdio.controller;

import lombok.RequiredArgsConstructor;
import org.example.cdio.repository.RoleRepository;
import org.example.cdio.repository.StoreRepository;
import org.example.cdio.repository.UserRepository;
import org.example.cdio.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final StoreRepository storeRepo;
    private final UserService userService;

    @GetMapping
    public String list(Model model) {

        // Ẩn ADMIN (role_id = 2)
        model.addAttribute("users",
                userRepo.findByRoleIdNot(2L));

        return "admin/user-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("roles",
                roleRepo.findAll()
                        .stream()
                        .filter(r -> r.getId() != 2L)
                        .toList());

        model.addAttribute("stores", storeRepo.findAll());
        model.addAttribute("user", null);

        return "admin/user-form";
    }
    @PostMapping("/create")
    public String create(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam Long roleId,
            @RequestParam(required = false) Long storeId
    ) {

        userService.createUser(username, password, fullName, roleId, storeId);
        return "redirect:/admin/users";
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {

        var user = userRepo.findById(id)
                .orElseThrow();


        if (user.getRole().getId() == 2L) {
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("stores", storeRepo.findAll());

        return "admin/user-form";
    }

    @PostMapping("/update")
    public String update(
            @RequestParam Long id,
            @RequestParam String fullName,
            @RequestParam Long roleId,
            @RequestParam(required = false) Long storeId
    ) {

        userService.updateUser(id, fullName, roleId, storeId);

        return "redirect:/admin/users";
    }

    @GetMapping("/lock/{id}")
    public String lock(@PathVariable Long id) {
        userService.lock(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/unlock/{id}")
    public String unlock(@PathVariable Long id) {
        userService.unlock(id);
        return "redirect:/admin/users";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        userService.deleteUser(id);

        return "redirect:/admin/users";
    }
}