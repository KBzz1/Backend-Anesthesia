package com.medical.pojo.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Size(min = 4, max = 255, message = "Minimum username length: 4 characters")
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Size(min = 8, max = 255, message = "Minimum password length: 8 characters")
    @Column(nullable = false)
    private String password;

    @Column(name = "staff_id")
    private Long staffId;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles;

    public User() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public void addRoles(Role... rolesToAdd) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.addAll(Arrays.asList(rolesToAdd));
    }
}
