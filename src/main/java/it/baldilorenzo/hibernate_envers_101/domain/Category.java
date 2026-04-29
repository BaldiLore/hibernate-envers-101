package it.baldilorenzo.hibernate_envers_101.domain;

import jakarta.persistence.*;

/**
 * Non-audited entity: shows how to handle a relationship from an audited
 * entity to a non-audited one without errors (see Product#category).
 */
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
