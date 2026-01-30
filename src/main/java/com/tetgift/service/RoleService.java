package com.tetgift.service;

import com.tetgift.dto.request.RoleRequest;
import com.tetgift.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getAllRoles();
    RoleResponse createRole(RoleRequest role);
    RoleResponse updateRole(Long id, RoleRequest role);
    void deleteRole(Long id);
    RoleResponse getRoleById(Long id);
}
