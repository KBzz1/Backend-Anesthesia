package com.medical.security;

import com.medical.pojo.auth.User;
import com.medical.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
            .map((role) -> new SimpleGrantedAuthority(role.getAuthority()))
            .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
            usernameOrEmail,
            user.getPassword(),
            authorities
        );
    }
}
