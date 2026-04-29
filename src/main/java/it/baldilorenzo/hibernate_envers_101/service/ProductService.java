package it.baldilorenzo.hibernate_envers_101.service;

import it.baldilorenzo.hibernate_envers_101.audit.CustomRevisionEntity;
import it.baldilorenzo.hibernate_envers_101.domain.Category;
import it.baldilorenzo.hibernate_envers_101.domain.Product;
import it.baldilorenzo.hibernate_envers_101.dto.RevisionDto;
import it.baldilorenzo.hibernate_envers_101.repository.CategoryRepository;
import it.baldilorenzo.hibernate_envers_101.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product create(Product product, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NoSuchElementException("Category " + categoryId));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    public Product updatePrice(Long id, BigDecimal newPrice) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product " + id));
        product.setPrice(newPrice);
        return product;
    }

    /**
     * Updates only the stock quantity: since it is @NotAudited it does NOT
     * create a new revision in PRODUCT_AUD. Verify after calling the endpoint:
     * the history of price/name/code does not grow.
     */
    public Product updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product " + id));
        product.setStockQuantity(quantity);
        return product;
    }

    /**
     * Example: filters revisions by criteria.
     * Here we only show MOD revisions (REVTYPE = MOD) with price > threshold.
     */
    @Transactional(readOnly = true)
    public List<RevisionDto<Product>> findPriceChangesAbove(Long productId, BigDecimal threshold) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = reader.createQuery()
                .forRevisionsOfEntity(Product.class, false, true)
                .add(AuditEntity.id().eq(productId))
                .add(AuditEntity.revisionType().eq(RevisionType.MOD))
                .add(AuditEntity.property("price").gt(threshold))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        return rows.stream().map(row -> {
            Product entity = (Product) row[0];
            CustomRevisionEntity rev = (CustomRevisionEntity) row[1];
            RevisionType type = (RevisionType) row[2];
            return new RevisionDto<>(
                    rev.getId(),
                    rev.getRevisionDate().toInstant(),
                    rev.getUsername(),
                    type.name(),
                    entity
            );
        }).toList();
    }

    /**
     * Example: all Product entities valid at a specific revision,
     * filtered by category. Useful for reporting snapshots.
     */
    @Transactional(readOnly = true)
    public List<Product> snapshotByCategory(Number revision, String categoryName) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Product> result = reader.createQuery()
                .forEntitiesAtRevision(Product.class, revision)
                .add(AuditEntity.relatedId("category")
                        .eq(resolveCategoryId(categoryName)))
                .getResultList();

        return result;
    }

    private Long resolveCategoryId(String name) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(Category::getId)
                .orElse(-1L);
    }
}
