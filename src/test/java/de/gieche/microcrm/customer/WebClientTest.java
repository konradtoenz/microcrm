package de.gieche.microcrm.customer;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
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

        HtmlPage viewCustomerPage = createNewCustomerForm.getButtonByName("save").click();
        assertThat(viewCustomerPage.asText()).contains(customer.getName());
        assertThat(viewCustomerPage.asText()).contains(customer.getStatus().toString());

        for (String note : customer.getNotes()) {
            createNote(viewCustomerPage, note);
        }

        String path = viewCustomerPage.getUrl().getPath();

        return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
    }

    private static void createNote(final HtmlPage viewCustomerPage, String note) throws IOException {
        HtmlPage addNotePage = viewCustomerPage.getAnchorByName("add_note_anchor").click();
        assertThat(addNotePage.asText()).contains("Add Note");

        HtmlForm createNewNoteForm = addNotePage.getFormByName("new_note");
        createNewNoteForm.getTextAreaByName("note").type(note);
        HtmlPage page = ((HtmlButton) addNotePage.getByXPath("//button[text() = 'Submit']").get(0)).click();
        assertThat(page.asText()).contains(note);
    }
}
