package it.baldilorenzo.hibernate_envers_101.web;

import it.baldilorenzo.hibernate_envers_101.domain.Category;
import it.baldilorenzo.hibernate_envers_101.domain.Product;
import it.baldilorenzo.hibernate_envers_101.dto.RevisionDto;
import it.baldilorenzo.hibernate_envers_101.repository.CategoryRepository;
import it.baldilorenzo.hibernate_envers_101.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductService productService,
                             CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody Category body) {
        return categoryRepository.save(body);
    }

    @PostMapping
    public Product create(@RequestBody Product body,
                          @RequestParam(required = false) Long categoryId) {
        return productService.create(body, categoryId);
    }

    @PutMapping("/{id}/price")
    public Product updatePrice(@PathVariable Long id, @RequestParam BigDecimal value) {
        return productService.updatePrice(id, value);
    }

    /** Updates only the stock: does NOT create a new revision. */
    @PutMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id, @RequestParam Integer value) {
        return productService.updateStock(id, value);
    }

    @GetMapping("/{id}/price-changes")
    public List<RevisionDto<Product>> priceChanges(@PathVariable Long id,
                                                   @RequestParam BigDecimal threshold) {
        return productService.findPriceChangesAbove(id, threshold);
    }

    @GetMapping("/snapshot")
    public List<Product> snapshot(@RequestParam Number revision,
                                  @RequestParam String category) {
        return productService.snapshotByCategory(revision, category);
    }
}
