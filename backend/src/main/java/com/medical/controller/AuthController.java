package com.medical.controller;

import com.medical.pojo.DTO.*;
import com.medical.repository.UserRepository;
import com.medical.security.JwtTokenProvider;
import com.medical.service.auth.AuthService;
import com.medical.service.auth.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository, UserService userService, 
                         AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signup(@RequestBody SignUpRequest request) {
        userService.registerUser(request);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            request.getEmail(), null, new ArrayList<>());

        String jwtToken = jwtTokenProvider.generateToken(authentication);
        return ResponseEntity.ok(ApiResponse.success(new SignUpResponse(jwtToken), "注册成功"));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<SignInResponse>> signin(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(ApiResponse.success(new SignInResponse(
            authService.authUser(request)
        ), "登录成功"));
    }
}
