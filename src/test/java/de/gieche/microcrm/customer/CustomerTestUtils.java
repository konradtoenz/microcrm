package de.gieche.microcrm.customer;

import org.apache.commons.lang3.RandomUtils;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.random;

final class CustomerTestUtils {

    private CustomerTestUtils() {
        throw new AssertionError("Utility class - do not instantiate.");
    }

    static Customer randomCustomer() {
        Customer customer = new Customer();
        customer.setName(random(16));
        customer.setStatus(randomStatus());
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

    private static CustomerStatus randomStatus() {
        return CustomerStatus.values()[RandomUtils.nextInt(0, CustomerStatus.values().length - 1)];
    }
}
