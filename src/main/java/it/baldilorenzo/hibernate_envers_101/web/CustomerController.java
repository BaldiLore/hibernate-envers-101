package it.baldilorenzo.hibernate_envers_101.web;

import it.baldilorenzo.hibernate_envers_101.domain.Address;
import it.baldilorenzo.hibernate_envers_101.domain.Customer;
import it.baldilorenzo.hibernate_envers_101.dto.RevisionDto;
import it.baldilorenzo.hibernate_envers_101.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public Customer create(@RequestBody Customer body) {
        return customerService.create(body);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Long id, @RequestBody Customer body) {
        return customerService.update(id, body);
    }

    @PostMapping("/{id}/addresses")
    public Customer addAddress(@PathVariable Long id, @RequestBody Address body) {
        return customerService.addAddress(id, body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Long id) {
        return customerService.findById(id);
    }

    // ========== History endpoints ==========

    /** Full history with the entity state at each revision. */
    @GetMapping("/{id}/history")
    public List<RevisionDto<Customer>> history(@PathVariable Long id) {
        return customerService.getHistory(id);
    }

    /** Just the list of revision numbers. */
    @GetMapping("/{id}/revisions")
    public List<Number> revisions(@PathVariable Long id) {
        return customerService.getRevisionNumbers(id);
    }

    /** Revision metadata: id, date, username. */
    @GetMapping("/{id}/revisions/meta")
    public List<RevisionDto<Void>> revisionsMeta(@PathVariable Long id) {
        return customerService.getRevisionMetadata(id);
    }

    /** State of the entity at a specific revision. */
    @GetMapping("/{id}/revisions/{rev}")
    public Customer atRevision(@PathVariable Long id, @PathVariable Number rev) {
        return customerService.getAtRevision(id, rev);
    }

    /**
     * State at an ISO-8601 timestamp, e.g.:
     * GET /api/customers/1/at?when=2026-04-29T12:00:00Z
     */
    @GetMapping("/{id}/at")
    public Customer atDate(@PathVariable Long id, @RequestParam Instant when) {
        return customerService.getAtDate(id, when);
    }

    /** Diff between two revisions: key = field name, value = [before, after]. */
    @GetMapping("/{id}/diff")
    public Map<String, Object[]> diff(@PathVariable Long id,
                                      @RequestParam Number from,
                                      @RequestParam Number to) {
        return customerService.diff(id, from, to);
    }

    /** Application-level rollback: re-applies the state of a past revision. */
    @PostMapping("/{id}/revert/{rev}")
    public Customer revert(@PathVariable Long id, @PathVariable Number rev) {
        return customerService.revertTo(id, rev);
    }
}
