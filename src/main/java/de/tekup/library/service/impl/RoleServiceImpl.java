package de.tekup.library.service.impl;


import de.tekup.library.dto.request.RoleRequest;
import de.tekup.library.dto.response.RoleResponse;
import de.tekup.library.entity.Authority;
import de.tekup.library.exception.AuthorityServiceException;
import de.tekup.library.exception.RoleServiceException;
import de.tekup.library.repository.RoleRepository;
import de.tekup.library.service.AuthorityService;
import de.tekup.library.service.RoleService;
import de.tekup.library.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final AuthorityService authorityService;
    
    @Override
    public RoleResponse saveRole(RoleRequest roleRequest) throws RoleServiceException {
        try {
            if (roleRepository.existsByRoleName(roleRequest.getRoleName())) {
                throw new RoleServiceException("Role already exists");
            }
            
            Collection<Authority> authorities = roleRequest.getAuthorities();
            
            if (authorities == null || authorities.isEmpty()) {
                // Handle default authority case
                roleRequest.setAuthorities(Collections.singletonList(authorityService.createDefaultAuthority()));
            } else {
                Set<String> authorityNames = authorities.stream()
                        .map(Authority::getName)
                        .collect(Collectors.toSet());
                
                // Check for the existence of all authorities using AuthorityService
                List<String> nonExistentAuthorities = authorityService.findNonExistentAuthorities(authorityNames);
                if (!nonExistentAuthorities.isEmpty()) {
                    throw new AuthorityServiceException("Authorities not found: " + nonExistentAuthorities);
                }
            }
            
            return Mapper.roleToRoleResponse(roleRepository.save(Mapper.roleRequestToRole(roleRequest)));
        } catch (RoleServiceException exception) {
            log.error("RoleService::saveRole, " + exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("RoleService::saveRole, " + exception.getMessage());
            throw new RoleServiceException(exception.getMessage());
        }
    }
    
    @Override
    public List<RoleResponse> findRoles() {
        try {
            return roleRepository
                    .findAll()
                    .stream()
                    .map(Mapper::roleToRoleResponse)
                    .toList();
            
        } catch (Exception exception) {
            log.error("RoleService::findRoles, " + exception.getMessage());
            throw new RoleServiceException(exception.getMessage());
        }
    }
}
