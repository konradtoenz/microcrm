package de.gieche.microcrm.customer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static de.gieche.microcrm.customer.CustomerTestUtils.randomCustomer;
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
}
