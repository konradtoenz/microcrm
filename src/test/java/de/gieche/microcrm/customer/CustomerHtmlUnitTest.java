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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static de.gieche.microcrm.customer.CustomerStatus.CURRENT;
import static de.gieche.microcrm.customer.CustomerStatus.NON_ACTIVE;
import static de.gieche.microcrm.customer.CustomerStatus.PROSPECTIVE;
import static de.gieche.microcrm.customer.CustomerTestUtils.randomCustomer;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
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
        createCustomer(customer);

        HtmlPage listCustomersPage = gotoCustomerListPage();
        HtmlTableRow row = (HtmlTableRow)
                listCustomersPage.getByXPath("//tr[td = '" + customer.getName() + "']").get(0);
        String pageContent = row.<HtmlPage>click().asText();
        assertThat(pageContent).contains(customer.getName());
        assertThat(pageContent).contains(customer.getStreet());
        assertThat(pageContent).contains(customer.getZipCode());
        assertThat(pageContent).contains(customer.getCity());
        assertThat(pageContent).contains(customer.getStatus().name());
        for (String note : customer.getNotes()) {
            assertThat(pageContent).contains(note);
        }
    }

    @Test
    public void should_set_status() throws Exception {
        long id = createCustomer(randomCustomer());

        setStatus(id, gotoCustomerDetailsPage(id), CURRENT);
        setStatus(id, gotoCustomerDetailsPage(id), NON_ACTIVE);
        setStatus(id, gotoCustomerDetailsPage(id), PROSPECTIVE);
    }

    @Test
    public void should_edit_note() throws Exception {
        HashSet<String> existingNotes = new HashSet<>();
        String existingNote = random(128);
        existingNotes.add(existingNote);
        long id = createCustomer(CustomerTestUtils.randomCustomer(existingNotes));

        HtmlPage viewCustomerPage = gotoCustomerDetailsPage(id);
        HtmlPage addNotePage = viewCustomerPage.getAnchorByText("Edit").click();
        assertThat(addNotePage.asText()).contains("Edit Note");
        assertThat(addNotePage.asText()).contains(existingNote);

        HtmlForm createNewNoteForm = addNotePage.getFormByName("new_note");
        String newNote = randomAlphabetic(60) + " " + random(60);
        createNewNoteForm.getTextAreaByName("note").setText(newNote);
        viewCustomerPage = ((HtmlButton) addNotePage.getByXPath("//button[text() = 'Submit']").get(0)).click();

        assertThat(viewCustomerPage.asText()).contains(newNote);

        Set<String> notes = customerWithId(id).getNotes();
        assertThat(notes.size()).isEqualTo(1);
        assertThat(notes.iterator().next()).isEqualTo(newNote);
    }

    @Test
    public void should_sort_by_name() throws Exception {
        should_sort(Customer::getName, "Name");
    }

    @Test
    public void should_sort_by_street() throws Exception {
        should_sort(Customer::getStreet, "Street");
    }

    @Test
    public void should_sort_by_zip_code() throws Exception {
        should_sort(Customer::getZipCode, "Zip Code");
    }

    @Test
    public void should_sort_by_city() throws Exception {
        should_sort(Customer::getCity, "City");
    }

    private void should_sort(Function<Customer, String> getSortedValue, String sortedByName) throws Exception {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            Customer customer = randomCustomer();
            createCustomer(customer);
            values.add(getSortedValue.apply(customer));
        }
        Collections.sort(values);

        String pageContent = gotoCustomerListPage().getAnchorByText(sortedByName).<HtmlPage>click().asText();
        for (int i = 1; i < values.size(); i++) {
            assertThat(pageContent.indexOf(values.get(i)))
                    .isGreaterThan(pageContent.indexOf(values.get(i - 1)));
        }
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
