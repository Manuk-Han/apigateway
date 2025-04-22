package com.example.apigateway.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("ROLE_ADMIN"), MANAGER("ROLE_MANAGER"), USER("ROLE_USER"), GUEST("ROLE_GUEST");

    private final String roleName;

    public static List<Role> getRoles(Role role) {
        List<Role> roles = new ArrayList<>();

        for (Role managerRole : Role.values()) {
            if(role.compareTo(managerRole) <= 0)
                roles.add(managerRole);
        }

        return roles;
    }

    public static Role findRole(String roleName) {
        for (Role role : Role.values()) {
            if (role.getRoleName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }

    public static boolean containsRole(Role updateRole) {
        for (Role managerRole : Role.values()) {
            if (managerRole.getRoleName().equals(updateRole.getRoleName())) {
                return !managerRole.equals(GUEST);
            }
        }
        return false;
    }
}
