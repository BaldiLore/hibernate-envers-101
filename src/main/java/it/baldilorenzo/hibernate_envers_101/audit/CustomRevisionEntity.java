package it.baldilorenzo.hibernate_envers_101.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

/**
 * Custom REVINFO table: in addition to id and timestamp (inherited from
 * DefaultRevisionEntity) it stores the username that made the change.
 *
 * @RevisionEntity registers the listener that fills the extra fields on every
 * commit in which at least one @Audited entity is modified.
 */
@Entity
@Table(name = "REVINFO")
@RevisionEntity(CustomRevisionListener.class)
public class CustomRevisionEntity extends DefaultRevisionEntity {

    @Column(name = "username", length = 100)
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
