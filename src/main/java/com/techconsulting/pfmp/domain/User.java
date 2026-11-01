package com.techconsulting.pfmp.domain;

import com.techconsulting.pfmp.utility.OtherDetailsConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "UK_User_username" ,columnNames = {"username"}),
        @UniqueConstraint(name = "UK_User_email" ,columnNames = {"email"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @Column(unique = true)
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Column(unique = true)
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id",foreignKey = @ForeignKey(name = "FK_User_user")),
            inverseJoinColumns = @JoinColumn(name = "role_id",foreignKey = @ForeignKey(name = "FK_User_role"))
    )
    private Set<Role> roles = new HashSet<>();

    @Convert(converter = OtherDetailsConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> otherDetails;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime dateCreated;
    @LastModifiedDate
    @Column(nullable = false)
    private OffsetDateTime lastUpdated;
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;
    @LastModifiedBy
    @Column(nullable = false)
    private String updatedBy;

    public User() {
    }

    public User(String name, String username, String email, String password) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}