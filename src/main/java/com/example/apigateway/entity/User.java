package com.example.apigateway.entity;

import com.example.apigateway.common.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String accountId;

    private String password;

    private String email;

    private boolean withdraw;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CourseStudent> courseStudentList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserRole> userRoles;

    public Set<Role> getRoles() {
        return this.userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

    public void addRole(UserRole userRole) {
        this.userRoles.add(userRole);
    }

    public void removeRole(UserRole userRole) {
        this.userRoles.remove(userRole);
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateWithdraw() {
        this.withdraw = !this.withdraw;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        this.getUserRoles().forEach(role -> {
            String roleName = role.getRole().getRoleName();
            authorities.add(new SimpleGrantedAuthority(roleName));
        });
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }
}
