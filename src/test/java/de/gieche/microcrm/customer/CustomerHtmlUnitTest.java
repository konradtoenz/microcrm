package de.gieche.microcrm.customer;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import org.assertj.core.api.ObjectAssert;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.gieche.microcrm.customer.CustomerStatus.CURRENT;
import static de.gieche.microcrm.customer.CustomerStatus.NON_ACTIVE;
import static de.gieche.microcrm.customer.CustomerStatus.PROSPECTIVE;
import static de.gieche.microcrm.customer.CustomerTestUtils.randomCustomer;
import static org.apache.commons.lang3.RandomStringUtils.random;
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

    @Test
    public void should_add_note() throws Exception {
        Customer customer = randomCustomer();
        customer.setNotes(new HashSet<>());
        long id = createCustomer(customer);
        String note = random(128);

        HtmlPage viewCustomerPage = gotoCustomerDetailsPage(id);
        HtmlPage addNotePage = viewCustomerPage.getAnchorByName("add_note_anchor").click();

        HtmlForm createNewNoteForm = addNotePage.getFormByName("new_note");
        createNewNoteForm.getTextAreaByName("note").type(note);
        viewCustomerPage = ((HtmlButton) addNotePage.getByXPath("//button[text() = 'Submit']").get(0)).click();

        assertThat(viewCustomerPage.asText()).contains(note);

        Set<String> notes = customerWithId(id).getNotes();
        assertThat(notes.size()).isEqualTo(1);
        assertThat(notes.iterator().next()).isEqualTo(note);
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
