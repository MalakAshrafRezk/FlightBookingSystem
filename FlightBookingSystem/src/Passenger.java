import java.util.Date;

class Passenger {
    private String name, passportNumber, specialRequests, passengerId;
    private Date dateOfBirth;

    public Passenger(String passengerId, String name, String passportNumber, Date dateOfBirth, String specialRequests) {
        this.passengerId = passengerId;
        this.name = name;
        this.passportNumber = passportNumber;
        this.dateOfBirth = dateOfBirth;
        this.specialRequests = specialRequests;
    }

    public String getPassengerId() { return passengerId; }
    public String getName() { return name; }
    public String getPassportNumber() { return passportNumber; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public String getSpecialRequests() { return specialRequests; }

    public String getPassengerDetails() {
        return "Name: " + name + ", Passport: " + passportNumber + ", DOB: " + dateOfBirth + ", Special Requests: " + specialRequests;
    }
}
