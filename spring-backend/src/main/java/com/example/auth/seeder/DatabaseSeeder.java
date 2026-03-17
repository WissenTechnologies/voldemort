package com.example.auth.seeder;

import com.example.auth.models.ERole;
import com.example.auth.models.Role;
import com.example.auth.models.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedUsers();
    }

    private void seedRoles() {
        if (!roleRepository.findByName(ERole.ROLE_USER).isPresent()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            System.out.println("Seeded ROLE_USER...");
        }
        if (!roleRepository.findByName(ERole.ROLE_MODERATOR).isPresent()) {
            roleRepository.save(new Role(ERole.ROLE_MODERATOR));
            System.out.println("Seeded ROLE_MODERATOR...");
        }
        if (!roleRepository.findByName(ERole.ROLE_ADMIN).isPresent()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("Seeded ROLE_ADMIN...");
        }
    }

    private void seedUsers() {
        if (!userRepository.findByEmail("admin@voldemort.app").isPresent()) {
            // Create Admin User
            User admin = new User("admin", "admin@voldemort.app", encoder.encode("admin123"));
            Set<Role> adminRoles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);
            userRepository.save(admin);
            System.out.println("Seeded admin account.");
        }

        if (!userRepository.findByEmail("user@voldemort.app").isPresent()) {
            // Create Standard User
            User user = new User("user", "user@voldemort.app", encoder.encode("user123"));
            Set<Role> userRoles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            userRoles.add(userRole);
            user.setRoles(userRoles);
            userRepository.save(user);
            System.out.println("Seeded user account.");
        }
    }
}
