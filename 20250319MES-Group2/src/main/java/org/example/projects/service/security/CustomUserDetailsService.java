package org.example.projects.service.security;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.example.projects.domain.UserRole;
import org.example.projects.repository.UserRoleRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    /**
     * 로그인 시에 무조건 실행되는 메소드
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user: " + username);
        UserRole userRole = userRoleRepository.findByUserName(username)
                .orElseThrow(() -> {
                    log.error("User not found: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.info("User found: " + userRole.getUserName());
        return User.builder()
                .username(userRole.getUserName())
                .password(userRole.getPassword())
                .roles(userRole.getRole())
                .build();
    }
}
