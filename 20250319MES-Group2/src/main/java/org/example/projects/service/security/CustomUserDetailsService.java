//package org.example.projects.service.security;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Log4j2
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//    private final PasswordEncoder passwordEncoder;
//
//    /**
//     * 로그인 시에 무조건 실행되는 메소드
//     * @param username
//     * @return
//     * @throws UsernameNotFoundException
//     */
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        log.info("loadUserByUsername: " + username);
//
//        String pw = passwordEncoder.encode("123");     // 1111 평문을 암호화함
//        log.info("123 암호화값: " + pw);
//
//        UserDetails userDetails = User.builder()
//                .username("user1")
//                .password(pw)
//                .authorities("ROLE_USER")
//                .build();
//
//        return userDetails;
//    }
//}
