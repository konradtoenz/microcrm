package de.gieche.microcrm.customer;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Date;

import static de.gieche.microcrm.customer.CustomerTestUtils.randomCustomer;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
@FlywayTest
public class CustomerHtmlUnitTest {

    @Autowired
    private CustomerRepository customerRepository;

    @LocalServerPort
    protected int localServerPort;

    private WebClient webClient;

    @Before
    public void initWebClient() {
        this.webClient = new WebClient();
    }

    @After
    public void closeWebClient() {
        this.webClient.close();
    }

    @Test
    public void should_create_new_customer() throws Exception {
        Customer customer = createCustomer();

        assertThat(this.customerRepository.count()).isEqualTo(1);

        Customer createdCustomer = this.customerRepository.findAll().iterator().next();
        assertThat(createdCustomer.getName()).isEqualTo(customer.getName());
        assertThat(createdCustomer.getId()).isPositive();
        assertThat(createdCustomer.getCreatedOn()).isBefore(new Date());
    }

    private Customer createCustomer() throws Exception {
        Customer customer = randomCustomer();

        HtmlPage createNewCustomerPage = this.webClient.getPage("http://localhost:"  + this.localServerPort  +"/customer/new");
        HtmlForm createNewCustomerForm = createNewCustomerPage.getFormByName("new_customer");
        createNewCustomerForm.getInputByName("name").type(customer.getName());

        HtmlPage customerCreatedPage = createNewCustomerForm.getButtonByName("save").click();
        assertThat(customerCreatedPage.asText()).contains(customer.getName());

        return customer;
    }
}
