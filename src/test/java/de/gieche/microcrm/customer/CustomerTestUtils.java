package de.gieche.microcrm.customer;

import org.apache.commons.lang3.RandomUtils;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

final class CustomerTestUtils {

    private CustomerTestUtils() {
        throw new AssertionError("Utility class - do not instantiate.");
    }

    static Customer randomCustomer() {
        Customer customer = new Customer();
        customer.setName(randomAlphabetic(16));
        customer.setStreet(randomAlphabetic(16) + randomNumeric(2));
        customer.setZipCode(randomNumeric(5));
        customer.setCity(randomAlphabetic(12));
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
}
