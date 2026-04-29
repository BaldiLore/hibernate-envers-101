package it.baldilorenzo.hibernate_envers_101.repository;

import it.baldilorenzo.hibernate_envers_101.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
