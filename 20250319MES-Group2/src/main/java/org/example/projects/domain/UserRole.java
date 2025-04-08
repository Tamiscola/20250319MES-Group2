package org.example.projects.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "user_role")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    private String role;
    private String email;

    @Column(name = "social_provider")
    private String socialProvider;

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "registered_by")
    private String registeredBy;

    @Column(name = "registered_date")
    private Date registeredDate;

    public UserRole(String socialId, String socialProvider, String userName) {
        this.socialId = socialId;
        this.socialProvider = socialProvider;
        this.userName = userName;
    }

    public UserRole() {}
}
