package com.FinalProject;

public class mUser {
    private String id, email, password, firstName, lastName, phone;
    private Float wage ;

    public mUser(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Float getWage() {
        return wage;
    }

    public void setWage(Float wage) {
        this.wage = wage;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof mUser))  {
            return false;
        }
        mUser other = (mUser) obj;
        return id.equals(other.id);
    }
    @Override
    public int hashCode() {
        return firstName.hashCode() + lastName.hashCode()
                +id.hashCode() + password.hashCode()
                + email.hashCode() + phone.hashCode() + wage.hashCode();
    }

}
