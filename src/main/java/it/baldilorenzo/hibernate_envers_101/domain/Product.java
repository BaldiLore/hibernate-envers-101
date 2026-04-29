package it.baldilorenzo.hibernate_envers_101.domain;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.math.BigDecimal;

/**
 * Selective auditing example:
 *
 * - {@code @NotAudited} on stockQuantity: stock changes very often and we
 *   don't want to clutter the audit table with those modifications.
 *
 * - {@code @Audited(targetAuditMode = NOT_AUDITED)} on the relation to
 *   Category: Category is not @Audited, so without this annotation Envers
 *   would throw an error at startup.
 */
@Entity
@Table(name = "product")
@Audited
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotAudited
    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Category category;

    public Product() {}

    public Product(String code, String name, BigDecimal price, Integer stockQuantity) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
