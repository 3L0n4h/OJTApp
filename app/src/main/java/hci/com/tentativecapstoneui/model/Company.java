package hci.com.tentativecapstoneui.model;

public class Company {
    String company;
    String address;
    String contactNo;
    String contactPerson;
    String emailAdd;

    public Company(String company, String address, String contactNo, String contactPerson, String emailAdd) {
        this.company = company;
        this.address = address;
        this.contactNo = contactNo;
        this.contactPerson = contactPerson;
        this.emailAdd = emailAdd;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmailAdd() {
        return emailAdd;
    }

    public void setEmailAdd(String emailAdd) {
        this.emailAdd = emailAdd;
    }
}
