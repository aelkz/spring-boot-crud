package com.aelkz.springboot.skeleton.model;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class User {

    public static enum Gender {
        M, F;
    }

    protected User() {}

    public User(@NotNull Long id, @NotNull @Size(min = 3, max = 255, message = "First name must have between 3 and 255 characters") String firstName, @NotNull String handle, @NotNull @Pattern(regexp = "[0-9]{3}[0-9]{3}[0-9]{3}[0-9]{2}", message = "Inform only numbers.") String cpf) {
        this.id = id;
        this.firstName = firstName;
        this.handle = handle;
        this.cpf = cpf;
    }

    public User(@NotNull @Size(min = 3, max = 255, message = "First name must have between 3 and 255 characters") String firstName, @NotNull @Size(min = 3, max = 255, message = "Last name must have between 3 and 255 characters") String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="user_id")
    private Long id;

    @NotNull
    @Size(min = 3, max = 255, message = "First name must have between 3 and 255 characters")
    @Column(name="first_name", nullable = false)
    private String firstName;

    @NotNull
    @Size(min = 3, max = 255, message = "Last name must have between 3 and 255 characters")
    @Column(name="last_name", nullable = false)
    private String lastName;

    @NotNull
    @Column(name="handle", nullable = false)
    private String handle;

    // Test: https://regex101.com/r/Zocxfa/1
    @NotNull
    @Pattern(regexp = "[0-9]{3}[0-9]{3}[0-9]{3}[0-9]{2}", message = "inform only numbers.")
    @Column(name="cpf", nullable = false)
    private String cpf;

    @NotNull
    @Column(name="date_of_birth", nullable = false)
    private Date dateOfBirth;

    @NotNull
    @Column(name="gender", nullable = false)
    private Gender gender;

    @Valid
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private Set<Address> addresses;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(Address address) {
        if (getAddresses() == null) {
            setAddresses(new HashSet<>());
        }
        getAddresses().add(address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(handle, user.handle) &&
                Objects.equals(cpf, user.cpf);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, firstName, handle, cpf);
    }
}
