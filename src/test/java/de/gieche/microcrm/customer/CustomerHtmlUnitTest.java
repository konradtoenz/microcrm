package de.gieche.microcrm.customer;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import org.assertj.core.api.ObjectAssert;
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
import java.util.List;

import static de.gieche.microcrm.customer.CustomerStatus.CURRENT;
import static de.gieche.microcrm.customer.CustomerStatus.NON_ACTIVE;
import static de.gieche.microcrm.customer.CustomerStatus.PROSPECTIVE;
import static de.gieche.microcrm.customer.CustomerTestUtils.randomCustomer;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
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
    @FlywayTest
    public void should_create_new_customer() throws Exception {
        Customer customer = randomCustomer();
        long id = createCustomer(customer);

        assertThat(this.customerRepository.count()).isEqualTo(1);

        Customer createdCustomer = customerWithId(id);
        assertThat(createdCustomer).isEqualToIgnoringGivenFields(customer, "id", "createdOn", "notes");
        assertThat(createdCustomer.getId()).isPositive();
        assertThat(createdCustomer.getCreatedOn()).isBefore(new Date());
    }

    @Test
    public void should_list_customers() throws Exception {
        createCustomer(randomCustomer());
        createCustomer(randomCustomer());
        createCustomer(randomCustomer());

        HtmlPage listCustomersPage = this.webClient.getPage("http://localhost:" + this.localServerPort + "/customers");
        for (Customer customer : this.customerRepository.findAll()) {
            assertThat(listCustomersPage.asText()).contains(customer.getName());
            assertThat(listCustomersPage.asText()).contains(customer.getStreet());
            assertThat(listCustomersPage.asText()).contains(customer.getZipCode());
            assertThat(listCustomersPage.asText()).contains(customer.getCity());
        }
    }

    @Test
    public void should_have_links_to_details_in_list() throws Exception {
        Customer customer = randomCustomer();
        customer.setStatus(PROSPECTIVE);
        createCustomer(customer);

        HtmlPage listCustomersPage = this.webClient.getPage("http://localhost:" + this.localServerPort + "/customers");
        HtmlTableRow row = (HtmlTableRow)
                listCustomersPage.getByXPath("//tr[td = '" + customer.getName() + "']").get(0);
        assertThat(findSetStatusButton(row.click(), CURRENT)).isNotNull();
    }

    @Test
    public void should_set_status() throws Exception {
        long id = createCustomer(randomCustomer());

        setStatus(id, viewCustomerPageForId(id), CURRENT);
        setStatus(id, viewCustomerPageForId(id), NON_ACTIVE);
        setStatus(id, viewCustomerPageForId(id), PROSPECTIVE);
    }

    private void setStatus(long customerId, HtmlPage viewCustomerPage, CustomerStatus setToStatus) throws Exception {
        HtmlButton statusButton = findSetStatusButton(viewCustomerPage, setToStatus);
        assertThat(statusButton).isNotNull();

        HtmlPage viewCustomerPageWithChangedStatus = statusButton.click();
        for (CustomerStatus customerStatus : CustomerStatus.values()) {
            ObjectAssert<HtmlButton> htmlButtonAssert = assertThat(findSetStatusButton(viewCustomerPageWithChangedStatus, customerStatus));
            if (customerStatus == setToStatus) {
                htmlButtonAssert.isNull();
            } else {
                htmlButtonAssert.isNotNull();
            }
        }
        assertThat(findSetStatusButton(viewCustomerPageWithChangedStatus, setToStatus)).isNull();

        Customer customerWithChangedStatus = customerWithId(customerId);
        assertThat(customerWithChangedStatus.getStatus()).isEqualTo(setToStatus);
    }

    private static HtmlButton findSetStatusButton(HtmlPage viewCustomerPage, CustomerStatus status) {
        HtmlButton statusButton = null;
        List<DomElement> statusButtons = viewCustomerPage.getElementsByName("status");
        for (DomElement domElement : statusButtons) {
            HtmlButton button = (HtmlButton) domElement;
            if (status.name().equals(button.getValueAttribute())) {
                statusButton = button;
            }
        }

        return statusButton;
    }

    private long createCustomer(Customer customer) throws java.io.IOException {
        HtmlPage createNewCustomerPage = this.webClient.getPage("http://localhost:" + this.localServerPort + "/customers/new");
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

    private Customer customerWithId(long id) {
        return this.customerRepository.findById(id)
                .orElseThrow(() -> new AssertionError("Customer with ID >" + id + "< could not be found."));
    }

    private HtmlPage viewCustomerPageForId(long id) throws java.io.IOException {
        return this.webClient.getPage("http://localhost:" + this.localServerPort + "/customers/" + id);
    }
}
