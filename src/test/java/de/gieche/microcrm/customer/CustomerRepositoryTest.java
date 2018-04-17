package de.gieche.microcrm.customer;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void should_create_id_and_creation_date_on_save() {
        Customer customer = randomCustomer();
        customerRepository.save(customer);

        assertThat(customerRepository.count()).isEqualTo(1);
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getCreatedOn()).isBefore(new Date());
    }

    private static Customer randomCustomer() {
        Customer customer = new Customer();
        customer.setName(random(16));
        customer.setStatus(randomStatus());
        customer.setNotes(randomNotes());

        return customer;
    }

    private static Set<String> randomNotes() {
        HashSet<String> notes = new HashSet<>();
        for (int i = 0; i < RandomUtils.nextInt(0, 9); i++) {
            notes.add(random(128));
        }

        return notes;
    }

    private static CustomerStatus randomStatus() {
        return CustomerStatus.values()[RandomUtils.nextInt(0, CustomerStatus.values().length - 1)];
    }
}
