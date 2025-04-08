package org.example.projects.service;

import org.example.projects.domain.UserRole;
import org.example.projects.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SocialLoginService {
    @Autowired
    private UserRoleRepository userRoleRepository;

    public UserRole login(String socialId, String socialProvider, String userName) {
        return userRoleRepository.findBySocialIdAndSocialProvider(socialId, socialProvider)
                .orElseGet(() -> {
                    UserRole newUser = new UserRole(socialId, socialProvider, userName);
                    return userRoleRepository.save(newUser);
                });
    }
}
