package com.nekromant.telegram.config;

import com.nekromant.telegram.model.Role;
import com.nekromant.telegram.model.UserInfo;
import com.nekromant.telegram.repository.RoleRepository;
import com.nekromant.telegram.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;


@Component
public class PrepopulateSecurityAccount {
    @Autowired
    private UserInfoService userInfoService;

    @Value("${owner.userName}")
    private String ownerUserName;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostConstruct
    public void prepoluateRole() {
        if (roleRepository.findByTitle("ROLE_admin") == null) {
            Role adminRole = new Role();
            adminRole.setTitle("ROLE_admin");
            roleRepository.save(adminRole);
        }
    }

    @PostConstruct
    public void prepoluateAccout() {
        UserInfo userInfo = userInfoService.getUserInfo(ownerUserName);
        userInfo.setPassword(passwordEncoder.encode("adminpass"));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByTitle("ROLE_admin"));
        userInfo.setRoles(roles);
        userInfoService.save(userInfo);

    }
}
