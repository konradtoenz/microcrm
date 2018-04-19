package de.gieche.microcrm.customer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.Set;

import static de.gieche.microcrm.customer.CustomerStatus.PROSPECTIVE;
import static javax.persistence.FetchType.EAGER;

@Entity
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String street;

    private String zipCode;

    private String city;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    @CreationTimestamp
    @SuppressWarnings("unused") // Automatically set by @CreationTimestamp.
    private Date createdOn;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status = PROSPECTIVE;

    @ElementCollection(fetch = EAGER)
    @Column(name = "note")
    private Set<String> notes;

    public Long getId() {
        return this.id;
    }

    public Date getCreatedOn() {
        // Doesn't return the actual object as it's mutable.
        return new Date(this.createdOn.getTime());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public CustomerStatus getStatus() {
        return this.status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public Set<String> getNotes() {
        return this.notes;
    }

    public void setNotes(Set<String> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("createdOn", createdOn)
                .append("status", status)
                .append("notes", notes)
                .toString();
    }
}
