package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.entities.Role;
import com.monkeyteam.monkeycloud.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    public Role getUserRole() {
        return roleRepository.findByName("ROLE_USER").get();
    }
}
