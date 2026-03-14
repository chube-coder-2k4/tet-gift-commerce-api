package com.tetgift.controller;

import com.tetgift.dto.request.RoleRequest;
import com.tetgift.dto.response.ResponseData;
import com.tetgift.dto.response.RoleResponse;
import com.tetgift.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role")
@Validated
@Slf4j
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new role", description = "Create a new role with the provided details")
    public ResponseEntity<ResponseData<RoleResponse>> createRole(@RequestBody @Valid RoleRequest roleRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED.value())
                .body(new ResponseData<RoleResponse>(
                        HttpStatus.CREATED.value(),
                        "Role created successfully",
                        roleService.createRole(roleRequest)
                        ));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing role", description = "Update the details of an existing role")
    public ResponseEntity<ResponseData<RoleResponse>> updateRole(@RequestParam Long id, @RequestBody @Valid RoleRequest roleRequest) {
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<RoleResponse>(
                        HttpStatus.OK.value(),
                        "Role updated successfully",
                        roleService.updateRole(id, roleRequest)
                ));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a role", description = "Delete a role by its ID")
    public ResponseEntity<ResponseData<Void>> deleteRole(@RequestParam Long id) {
        roleService.deleteRole(id);
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<Void>(
                        HttpStatus.OK.value(),
                        "Role deleted successfully",
                        null
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieve the details of a role by its ID")
    public ResponseEntity<ResponseData<RoleResponse>> getRoleById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<RoleResponse>(
                        HttpStatus.OK.value(),
                        "Role retrieved successfully",
                        roleService.getRoleById(id)
                ));
    }

    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieve a list of all roles")
    public ResponseEntity<ResponseData<Iterable<RoleResponse>>> getAllRoles() {
        return ResponseEntity
                .status(HttpStatus.OK.value())
                .body(new ResponseData<Iterable<RoleResponse>>(
                        HttpStatus.OK.value(),
                        "Roles retrieved successfully",
                        roleService.getAllRoles()
                ));
    }

}
