package de.gieche.microcrm.customer;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;

import static de.gieche.microcrm.customer.CustomerTestUtils.randomCustomer;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, FlywayTestExecutionListener.class })
abstract class WebClientTest {

    private static final String URL_PATTERN = "http://localhost:%s/customers/";

    @LocalServerPort
    private String localServerPort;

    @Autowired
    CustomerRepository customerRepository;

    private WebClient webClient;

    @Before
    public void instantiateWebClient() {
        this.webClient = new WebClient();
    }

    @After
    public void closeWebClient() {
        this.webClient.close();
    }

    HtmlPage gotoNewCustomerPage() throws IOException {
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

    Customer randomPersistedCustomer() {
        return this.customerRepository.save(randomCustomer());
    }

    static void createNote(final HtmlPage viewCustomerPage, String note) throws IOException {
        HtmlPage addNotePage = viewCustomerPage.getAnchorByName("add_note_anchor").click();
        assertThat(addNotePage.asText()).contains("Add Note");

        HtmlForm createNewNoteForm = addNotePage.getFormByName("new_note");
        createNewNoteForm.getTextAreaByName("note").type(note);
        HtmlPage page = ((HtmlButton) addNotePage.getByXPath("//button[text() = 'Submit']").get(0)).click();
        assertThat(page.asText()).contains(note);
    }

    Customer customerWithId(long id) {
        return this.customerRepository.findById(id)
                .orElseThrow(() -> new AssertionError("Customer with ID >" + id + "< could not be found."));
    }
}
