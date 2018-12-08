package hci.com.tentativecapstoneui.model;

public class CompanyR {

    private String companyName;
    private String companyAddres;
    private String contactPerson;
    private String contactTimePref;
    private String remark;

    private boolean isSelected;

    public void setSelected(boolean selection){
        this.isSelected = selection;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public CompanyR(String companyName, String companyAddres, String contactPerson, String contactTimePref, String remark) {
        this.companyName = companyName;
        this.companyAddres = companyAddres;
        this.contactPerson = contactPerson;
        this.contactTimePref = contactTimePref;
        this.remark = remark;
    }

    public CompanyR(String companyName, String companyAddres) {
        this.companyName = companyName;
        this.companyAddres = companyAddres;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddres() {
        return companyAddres;
    }

    public void setCompanyAddres(String companyAddres) {
        this.companyAddres = companyAddres;
    }

    public String getContactPerson() { return contactPerson;  }

    public void setContactPerson(String contactPerson) {    this.contactPerson = contactPerson;  }

    public String getContactTimePref() { return contactTimePref;  }

    public void setContactTimePref(String contactTimePref) { this.contactTimePref = contactTimePref;  }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
