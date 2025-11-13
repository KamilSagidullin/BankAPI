package app.api.bankapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usr")
@Data
@NoArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "fullName")
    private String fullName;
    @Column(name = "password")
    private String password;

    @ElementCollection(targetClass = RoleType.class)
    @JoinTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role",nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Set<RoleType> roles = new HashSet<>();

    public User(String fullName, String password, RoleType roleType) {
        this.fullName = fullName;
        this.password = password;
        roles.add(roleType);
    }
}
