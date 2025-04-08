package org.example.projects.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.CustomOAuth2User;
import org.example.projects.domain.UserRole;
import org.example.projects.repository.UserRoleRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRoleRepository userRoleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("userRequest.........");
        log.info(userRequest);
        log.info("oauth2 user.................");

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        String provider = clientRegistration.getRegistrationId();
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String socialId = null;
        String nickname = null;
        if("kakao".equals(provider)) {
            socialId = String.valueOf(attributes.get("id"));
            nickname = getKakaoNickname(attributes);
        } else if("naver".equals(provider)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            socialId = (String) response.get("id");
            nickname = (String) response.get("name");
        }
        log.info("=================================");
        log.info("provider: " + provider);
        log.info("socialId: " + socialId);
        log.info("nickname: " + nickname);
        log.info("attirbutes: " + attributes);
        log.info("=================================");

        Optional<UserRole> existUser = userRoleRepository.findBySocialId(socialId);

        if(existUser.isEmpty()) {
            UserRole newUser = new UserRole();
            newUser.setSocialId(socialId);
            newUser.setSocialProvider(provider);
            newUser.setUserName(nickname);
            newUser.setPassword("SOCIAL_LOGIN");
            newUser.setRegisteredBy(provider);
            newUser.setRegisteredDate(new Date());

            userRoleRepository.save(newUser);
            log.info("newUser: " + newUser.getUserName());
        } else {
            log.info("existUser: " + existUser.get().getUserName());
        }

        return new CustomOAuth2User(nickname, socialId, provider, attributes, oAuth2User.getAuthorities());
    }

    private String getKakaoNickname(Map<String, Object> paramMap) {
        log.info("KAKAO getKakaoNickname---------------------------");
        Object value = paramMap.get("kakao_account");

        if(value == null) return "Unknown User";

        LinkedHashMap accountMap = (LinkedHashMap) value;
        LinkedHashMap profileMap = (LinkedHashMap) accountMap.get("profile");
        String nickname = (String) profileMap.get("nickname");
        log.info("nickname..." + nickname);

        return nickname;
    }
}