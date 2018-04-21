package de.gieche.microcrm.customer;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
abstract class WebClientTest {

    private static final String URL_PATTERN = "http://localhost:%s/customers/";

    @LocalServerPort
    private String localServerPort;

    private WebClient webClient;

    @Before
    public void instantiateWebClient() {
        this.webClient = new WebClient();
    }

    @After
    public void closeWebClient() {
        this.webClient.close();
    }

    private HtmlPage gotoNewCustomerPage() throws IOException {
        return this.webClient.getPage(baseUrl() + "new");
    }

    HtmlPage gotoCustomerDetailsPage(long customerId) throws IOException {
        return this.webClient.getPage(baseUrl() + customerId);
    }

    HtmlPage gotoCustomerListPage() throws IOException {
        return this.webClient.getPage(baseUrl());
    }

    private String baseUrl() {
        return format(URL_PATTERN, this.localServerPort);
    }

    long createCustomer(Customer customer) throws java.io.IOException {
        HtmlPage createNewCustomerPage = gotoNewCustomerPage();
        HtmlForm createNewCustomerForm = createNewCustomerPage.getFormByName("new_customer");
        createNewCustomerForm.getInputByName("name").type(customer.getName());
        createNewCustomerForm.getInputByName("street").type(customer.getStreet());
        createNewCustomerForm.getInputByName("zipCode").type(customer.getZipCode());
        createNewCustomerForm.getInputByName("city").type(customer.getCity());

        HtmlPage customerCreatedPage = createNewCustomerForm.getButtonByName("save").click();
        assertThat(customerCreatedPage.asText()).contains(customer.getName());
        assertThat(customerCreatedPage.asText()).contains(customer.getStatus().toString());

        String path = customerCreatedPage.getUrl().getPath();

        return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
    }
}
