package com.example.backendproject01.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Form Login Security config
 *  Jwt Security Config 와 같이 사용 시 에러 (why? 한 번에 하나의 인증 방식만, 두 방식을 둘 다 사용하려면 명시적으로 분리하기!)
 *  ex 1. Bean 이름 구분 -> @Bean("sessionFilterChain"), @Bean("jwtFilterChain")
 *  ex 2. 조건 부 설정 -> if("jwt",equals(anytype)) {jwt} else {session}
 *  ex 3. 멀티 SecurityFilterChain  -> @Order(1) 우선순위 1 , @Order(2) 우선순쉬 2
 *  그냥 하나만 활성화 하기...
 * */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/join", "/login").permitAll()    // 지정한 경로(requestMatchers(...))는 인증 없이 접근 허용(permitAll())
                    .requestMatchers("/memo/**").authenticated()
                    .anyRequest().authenticated()                                 // 모든 요청(anyRequest())은 인증된 사용자(authenticated())만 접근 가능
                )
                .formLogin(form -> form
                        .loginPage("/login")            // Get -> 로그인 폼 페이지 보여주기
                        .loginProcessingUrl("/login")   // Post -> 실제 로그인 처리하기
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/memo")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/")
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID") // JSESSIONID 브라우저에 저장된 세션 식별 쿠키
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)                 // 동일한 계정으로 중복 로그인하면 기존 세션을 만료시키거나 거부
                        .maxSessionsPreventsLogin(false)    // 기존 세션이 있더라도 새로운 로그인은 허용하고 기존 세션을 만료
                )
                .csrf(csrf -> csrf.disable());

                return http.build();

    }
}
