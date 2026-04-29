package it.baldilorenzo.hibernate_envers_101.repository;

import it.baldilorenzo.hibernate_envers_101.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
