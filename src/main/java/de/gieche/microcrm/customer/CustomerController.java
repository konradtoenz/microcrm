package de.gieche.microcrm.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
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
    public String newCustomer(Customer customer) {
        return "customer/new";
    }

    @RequestMapping(path = "/{id}/notes/new", method = GET)
    public ModelAndView newNote(@PathVariable("id") String id, @RequestParam() Optional<String> existingNote) {
        ModelAndView modelAndView = new ModelAndView("customer/note/new", "customerId", id);
        existingNote.ifPresent(note -> modelAndView.addObject("existingNote", note));

        return modelAndView;
    }

    @RequestMapping(method = POST)
    public String saveCustomer(Customer customer) {
        return "redirect:/customers/" + this.customerRepository.save(customer).getId();
    }

    @RequestMapping(path = "/{id}", method = GET)
    public ModelAndView viewCustomer(@PathVariable long id) {
        Optional<Customer> customer = this.customerRepository.findById(id);
        return new ModelAndView("customer/view", "customer", customer.orElse(null));
    }

    @RequestMapping(method = GET)
    public ModelAndView listCustomers(
            @RequestParam(name = "sort_by") Optional<String> sortByParam,
            @RequestParam(name = "filter_by") Optional<String> filterByParam) {
        List<Customer> customers = new ArrayList<>();
        if (filterByParam.isPresent()) {
            Customer probe = new Customer();
            probe.setName(filterByParam.get());
            probe.setStreet(filterByParam.get());
            probe.setZipCode(filterByParam.get());
            probe.setCity(filterByParam.get());

            ExampleMatcher matcher =
                    ExampleMatcher.matchingAny().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                    .withIgnorePaths("createdOn", "status", "notes");
            Example<Customer> example = Example.of(probe, matcher);
            customers.addAll(this.customerRepository.findAll(example));
        } else {
            String sortBy = sortByParam.map(value -> LOWER_UNDERSCORE.to(LOWER_CAMEL, value)).orElse("name");
            customers.addAll(this.customerRepository.findAll(Sort.by(sortBy)));
        }

        return new ModelAndView("customer/list", "customers", customers);
    }

    @RequestMapping(path = "/{id}/status", method = POST)
    public String updateStatus(@PathVariable long id, CustomerStatus status) {
        Customer customer = this.customerRepository.findById(id).orElseThrow(RuntimeException::new);
        customer.setStatus(status);
        this.customerRepository.save(customer);

        return "redirect:/customers/" + id;
    }

    @RequestMapping(path = "/{id}/notes", method = POST)
    public String upsertNote(@PathVariable long id, String note, Optional<String> existingNote) {
        Customer customer = this.customerRepository.findById(id).orElseThrow(RuntimeException::new);

        Set<String> notes = customer.getNotes();
        existingNote.ifPresent(notes::remove);
        notes.add(note);

        this.customerRepository.save(customer);

        return "redirect:/customers/" + id;
    }
}
