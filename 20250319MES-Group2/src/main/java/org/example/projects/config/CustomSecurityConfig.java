package org.example.projects.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.example.projects.domain.UserRole;
import org.example.projects.repository.UserRoleRepository;
import org.example.projects.service.security.CustomUserDetailsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.sql.DataSource;


@Configuration
@Log4j2
@RequiredArgsConstructor
@EnableWebSecurity
// 권한을 사용하기 위한 어노테이션(prePostEnabled는 @PreAuthorize 혹은 @PostAuthorize 어노테이션을 사용하기 위한 속성)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CustomSecurityConfig {
    private final DataSource dataSource;    // MariaDB와 JAVA의 연결소스(고리)
    private final UserRoleRepository userRoleRepository;

    @Bean
    public CustomUserDetailsService securityCustomUserDetailsService() {
        return new CustomUserDetailsService(passwordEncoder(), userRoleRepository);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(securityCustomUserDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("---------------------config-----------------------------");

        httpSecurity.authenticationProvider(authenticationProvider())
                .formLogin()               // 폼 로그인 인증 페이지 접근(/login)
                .loginPage("/login")
                .loginProcessingUrl("/login") // Endpoint for form submission
                .defaultSuccessUrl("/plan/list", true)  // 사용자 로그인 페이지 설정;
                .failureUrl("/login?error=true") // Redirect after failed login
                .permitAll();;

        httpSecurity.csrf().disable();

        httpSecurity.rememberMe()
                .key("12345678")                                // 쿠키값을 인코딩하기 위한 key
                .tokenRepository(persistentTokenRepository())                          // token을 처리할 데이터 엑세스 관련
                .userDetailsService(securityCustomUserDetailsService())   // Spring Security에서 관리하는 서비스 주입
                .tokenValiditySeconds(60 * 60 * 24 * 30);       // 30일동안 보관(단위는 초)

        httpSecurity.authorizeRequests()
                .antMatchers("/login", "/oauth2/**", "/error").permitAll() // Allow access to login and error pages
                .antMatchers("/plan/**").hasRole("USER") // Restrict access to /plan/** to authenticated users
                .anyRequest().authenticated();

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

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);

        return repo;
    }

    @Bean
    public CommandLineRunner initializeUsers() {
        return args -> {
            if (userRoleRepository.count() == 0) {
                UserRole defaultUser = new UserRole();
                defaultUser.setUserName("user1");
                defaultUser.setPassword(passwordEncoder().encode("123"));
                defaultUser.setRole("USER");
                defaultUser.setEmail("user1@example.com");
                userRoleRepository.save(defaultUser);
                log.info("Default user created: user1");
            }
        };
    }
}
