package org.example.projects.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "persistent_logins")
public class PersistentLogin {
    @Id
    private String series;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String token;

    @Column(name = "last_used", nullable = false)
    private Timestamp lastUsed;
}
