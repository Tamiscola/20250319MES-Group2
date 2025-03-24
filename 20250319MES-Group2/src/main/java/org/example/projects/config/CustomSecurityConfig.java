package org.example.projects.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Log4j2
@RequiredArgsConstructor
// 권한을 사용하기 위한 어노테이션(prePostEnabled는 @PreAuthorize 혹은 @PostAuthorize 어노테이션을 사용하기 위한 속성)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("---------------------config-----------------------------");

        httpSecurity.formLogin();

        return httpSecurity.build();
    }

    // resources/static 폴더의 모든 파일들은 인증에서 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("-------------------------web configure--------------------");
        return (web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {  // 패스워드 암호화를 위한 spring bean
        return new BCryptPasswordEncoder();
    }
}
