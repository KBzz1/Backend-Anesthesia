package com.medical.service.auth;

import com.medical.pojo.DTO.SignUpRequest;
import com.medical.pojo.auth.User;
import com.medical.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(SignUpRequest request) {
        if (userRepository.existsByUsernameOrEmail(request.getEmail(), request.getEmail())) {
            throw new IllegalArgumentException("该用户名或邮箱已存在");
        }

        User newUser = new User();
        newUser.setUsername(request.getEmail());
        newUser.setEmail(request.getEmail());
        newUser.setName(request.getFirstName() + " " + request.getLastName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setStaffId(request.getStaffId());

        userRepository.save(newUser);
    }
}
