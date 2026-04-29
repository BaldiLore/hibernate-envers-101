package it.baldilorenzo.hibernate_envers_101.repository;

import it.baldilorenzo.hibernate_envers_101.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
