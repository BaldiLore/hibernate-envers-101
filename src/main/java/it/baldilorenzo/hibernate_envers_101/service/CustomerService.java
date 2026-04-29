package it.baldilorenzo.hibernate_envers_101.service;

import it.baldilorenzo.hibernate_envers_101.audit.CustomRevisionEntity;
import it.baldilorenzo.hibernate_envers_101.domain.Address;
import it.baldilorenzo.hibernate_envers_101.domain.Customer;
import it.baldilorenzo.hibernate_envers_101.dto.RevisionDto;
import it.baldilorenzo.hibernate_envers_101.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ========== CRUD ==========

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer update(Long id, Customer payload) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer " + id));
        existing.setFirstName(payload.getFirstName());
        existing.setLastName(payload.getLastName());
        existing.setEmail(payload.getEmail());
        return existing;
    }

    public Customer addAddress(Long customerId, Address address) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer " + customerId));
        customer.addAddress(address);
        return customer;
    }

    public void delete(Long id) {
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer " + id));
    }

    // ========== History ==========

    /**
     * Example 1: all revisions of an entity.
     * Uses forRevisionsOfEntity(..., true) -> returns triples
     * [entityState, revisionInfo, revisionType].
     */
    @Transactional(readOnly = true)
    public List<RevisionDto<Customer>> getHistory(Long customerId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = reader.createQuery()
                .forRevisionsOfEntity(Customer.class, false, true)
                .add(AuditEntity.id().eq(customerId))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        return rows.stream().map(row -> {
            Customer entity = (Customer) row[0];
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
     * Example 2: state of the entity at a specific revision.
     */
    @Transactional(readOnly = true)
    public Customer getAtRevision(Long customerId, Number revision) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        Customer c = reader.find(Customer.class, customerId, revision);
        if (c == null) {
            throw new NoSuchElementException(
                    "No version of Customer " + customerId + " at revision " + revision);
        }
        return c;
    }

    /**
     * Example 3: state of the entity at a given timestamp.
     * Envers looks up the active revision at that timestamp and returns
     * the corresponding state.
     */
    @Transactional(readOnly = true)
    public Customer getAtDate(Long customerId, Instant when) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        Number revAtDate = reader.getRevisionNumberForDate(Date.from(when));
        return getAtRevision(customerId, revAtDate);
    }

    /**
     * Example 4: comparison between two revisions of the same entity,
     * lists the fields that have changed.
     */
    @Transactional(readOnly = true)
    public Map<String, Object[]> diff(Long customerId, Number revFrom, Number revTo) {
        Customer a = getAtRevision(customerId, revFrom);
        Customer b = getAtRevision(customerId, revTo);

        Map<String, Object[]> changes = new java.util.LinkedHashMap<>();
        compare(changes, "firstName", a.getFirstName(), b.getFirstName());
        compare(changes, "lastName",  a.getLastName(),  b.getLastName());
        compare(changes, "email",     a.getEmail(),     b.getEmail());
        return changes;
    }

    private static void compare(Map<String, Object[]> sink, String field, Object before, Object after) {
        if (before == null ? after != null : !before.equals(after)) {
            sink.put(field, new Object[]{before, after});
        }
    }

    /**
     * Example 5: list of revision numbers (without loading the entity).
     * Handy to build a lightweight timeline.
     */
    @Transactional(readOnly = true)
    public List<Number> getRevisionNumbers(Long customerId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        return reader.getRevisions(Customer.class, customerId);
    }

    /**
     * Example 6: who and when modified the entity.
     */
    @Transactional(readOnly = true)
    public List<RevisionDto<Void>> getRevisionMetadata(Long customerId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Number> revs = reader.getRevisions(Customer.class, customerId);
        return revs.stream().map(r -> {
            CustomRevisionEntity rev =
                    reader.findRevision(CustomRevisionEntity.class, r);
            return new RevisionDto<Void>(
                    rev.getId(),
                    rev.getRevisionDate().toInstant(),
                    rev.getUsername(),
                    null,
                    null
            );
        }).collect(Collectors.toList());
    }

    /**
     * Example 7: application-level rollback.
     * Retrieves the state at a past revision and re-applies it as an UPDATE,
     * producing a new MOD revision.
     */
    public Customer revertTo(Long customerId, Number revision) {
        Customer snapshot = getAtRevision(customerId, revision);
        Customer current = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer " + customerId));
        current.setFirstName(snapshot.getFirstName());
        current.setLastName(snapshot.getLastName());
        current.setEmail(snapshot.getEmail());
        return current;
    }
}
