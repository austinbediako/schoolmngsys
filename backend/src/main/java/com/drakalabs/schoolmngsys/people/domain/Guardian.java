package com.drakalabs.schoolmngsys.people.domain;

import com.drakalabs.schoolmngsys.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Relationship type lives on the {@link StudentGuardian} link, not here (docs/02 §2). */
@Entity
@Table(name = "guardians")
public class Guardian extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "address")
    private String address;

    protected Guardian() {
    }

    public Guardian(String firstName, String lastName, String phone, String email, String occupation, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.occupation = occupation;
        this.address = address;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getAddress() {
        return address;
    }

    public void updateContact(String phone, String email, String occupation, String address) {
        this.phone = phone;
        this.email = email;
        this.occupation = occupation;
        this.address = address;
    }
}
