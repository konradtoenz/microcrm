package de.gieche.microcrm.customer;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import org.assertj.core.api.ObjectAssert;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static de.gieche.microcrm.customer.CustomerStatus.CURRENT;
import static de.gieche.microcrm.customer.CustomerStatus.NON_ACTIVE;
import static de.gieche.microcrm.customer.CustomerStatus.PROSPECTIVE;
import static de.gieche.microcrm.customer.CustomerTestUtils.randomCustomer;
import static org.assertj.core.api.Assertions.assertThat;

public class CustomerHtmlUnitTest extends WebClientTest {

    @Autowired
    private CustomerRepository customerRepository;

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
        assertThat(createdCustomer.getNotes()).isEqualTo(customer.getNotes());
    }

    @Test
    public void should_list_customers() throws Exception {
        createCustomer(randomCustomer());
        createCustomer(randomCustomer());
        createCustomer(randomCustomer());

        HtmlPage listCustomersPage = gotoCustomerListPage();
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

        HtmlPage listCustomersPage = gotoCustomerListPage();
        HtmlTableRow row = (HtmlTableRow)
                listCustomersPage.getByXPath("//tr[td = '" + customer.getName() + "']").get(0);
        assertThat(findSetStatusButton(row.click(), CURRENT)).isNotNull();
    }

    @Test
    public void should_set_status() throws Exception {
        long id = createCustomer(randomCustomer());

        setStatus(id, gotoCustomerDetailsPage(id), CURRENT);
        setStatus(id, gotoCustomerDetailsPage(id), NON_ACTIVE);
        setStatus(id, gotoCustomerDetailsPage(id), PROSPECTIVE);
    }

    private void setStatus(long customerId, HtmlPage viewCustomerPage, CustomerStatus setToStatus) throws Exception {
        HtmlButton statusButton = findSetStatusButton(viewCustomerPage, setToStatus);
        assertThat(statusButton).isNotNull();

        HtmlPage viewCustomerPageWithChangedStatus = statusButton.click();
        for (CustomerStatus customerStatus : CustomerStatus.values()) {
            ObjectAssert<HtmlButton> htmlButtonAssert =
                    assertThat(findSetStatusButton(viewCustomerPageWithChangedStatus, customerStatus));
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

    private Customer customerWithId(long id) {
        return this.customerRepository.findById(id)
                .orElseThrow(() -> new AssertionError("Customer with ID >" + id + "< could not be found."));
    }
}
