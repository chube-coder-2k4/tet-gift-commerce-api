package com.tetgift.service.impl;

import com.tetgift.dto.request.RoleRequest;
import com.tetgift.dto.response.RoleResponse;
import com.tetgift.exception.ResourceNotFoundException;
import com.tetgift.model.Role;
import com.tetgift.repository.jpa.RoleRepository;
import com.tetgift.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(role -> RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build()).toList();
    }

    @Override
    public RoleResponse createRole(RoleRequest role) {
        Role newRole = Role.builder()
                .name(role.getName())
                .description(role.getDescription())
                .build();
        Role savedRole = roleRepository.save(newRole);
        return RoleResponse.builder()
                .id(savedRole.getId())
                .name(savedRole.getName())
                .description(savedRole.getDescription())
                .build();
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest role) {
        Role existingRole = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        Role updatedRole = roleRepository.save(existingRole);
        return RoleResponse.builder()
                .id(updatedRole.getId())
                .name(updatedRole.getName())
                .description(updatedRole.getDescription())
                .build();
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        roleRepository.deleteById(id);
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
