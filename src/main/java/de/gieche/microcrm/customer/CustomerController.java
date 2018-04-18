package de.gieche.microcrm.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @RequestMapping(path = "/new", method = GET)
    @SuppressWarnings("unused") // Used by Spring MVC.
    public String newCustomer(Customer customer) {
        return "customer/new";
    }

    @RequestMapping(method = POST)
    @SuppressWarnings("unused") // Used by Spring MVC.
    public String saveCustomer(Customer customer) {
        return "redirect:/customers/" + this.customerRepository.save(customer).getId();
    }

    @RequestMapping(path = "/{id}", method = GET)
    @SuppressWarnings("unused") // Used by Spring MVC.
    public ModelAndView viewCustomer(@PathVariable long id) {
        Optional<Customer> customer = this.customerRepository.findById(id);
        return new ModelAndView("customer/view", "customer", customer.orElse(null));
    }

    @RequestMapping(path = "/{id}/status", method = POST)
    @SuppressWarnings("unused") // Used by Spring MVC.
    public String updateStatus(@PathVariable long id, CustomerStatus status) {
        Customer customer = this.customerRepository.findById(id).orElseThrow(RuntimeException::new);
        customer.setStatus(status);
        this.customerRepository.save(customer);

        return "redirect:/customers/" + id;
    }
}
