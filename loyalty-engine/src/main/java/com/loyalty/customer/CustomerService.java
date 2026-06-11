package com.loyalty.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findByActiveTrue(pageable)
                .map(CustomerDTO::from);
    }

    public CustomerDTO getCustomerById(Long id) {
        Customer customer = findActiveById(id);
        return CustomerDTO.from(customer);
    }

    public CustomerDTO updateCustomer(Long id, CustomerUpdateRequest request) {
        Customer customer = findActiveById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            customer.setName(request.getName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customer.setPhone(request.getPhone());
        }

        return CustomerDTO.from(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        Customer customer = findActiveById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    // ---- helpers ----

    private Customer findActiveById(Long id) {
        return customerRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));
    }
}
