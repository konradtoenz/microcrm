package de.gieche.microcrm.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/customer")
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
    @ResponseBody
    @SuppressWarnings("unused") // Used by Spring MVC.
    public String saveCustomer(Customer customer) {
        return this.customerRepository.save(customer).toString();
    }
}
