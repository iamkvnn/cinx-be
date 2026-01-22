package com.cinx.auth.service.user;

import com.cinx.auth.consts.Role;
import com.cinx.auth.dto.RegisterDto;
import com.cinx.auth.exception.AlreadyExistException;
import com.cinx.auth.model.User;
import com.cinx.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService, IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Loading user by email: " + email);
        User user = findByEmail(email);
        System.out.println("User found: " + user);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Set.of(() -> user.getRole().name())
        );
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public User createUser(RegisterDto user) {
        if (userRepository.existsByEmail(user.email())) {
            throw new AlreadyExistException("User already exists with email: " + user.email());
        }
        return userRepository.save(User
                .builder()
                .email(user.email())
                .name(user.name())
                .password(passwordEncoder.encode(user.password()))
                .role(Role.USER)
                .gender(user.gender())
                .build());
    }
}
