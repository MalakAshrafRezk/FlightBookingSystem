public abstract class User {

    private String userId, username, password, name, email, contactInfo;
    private int accessLevel;
    protected String role;

    public User(String userId, String username, String password, String name, String email, String contactInfo) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.contactInfo = contactInfo;
    }

    public User(String userId, String username, String password, String name, String email, String contactInfo, int accessLevel) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.contactInfo = contactInfo;
        this.accessLevel = accessLevel;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public boolean login(String u, String p) {
        return username.equals(u) && password.equals(p);
    }

    public void logout() {
        System.out.println("User " + username + " logged out.");
    }

    public void updateProfile(String name, String email, String contactInfo) {
        boolean updated = false;

        if (name != null && !name.equals(this.name)) {
            this.name = name;
            updated = true;
        }

        if (email != null && !email.equals(this.email)) {
            this.email = email;
            updated = true;
        }

        if (contactInfo != null && !contactInfo.equals(this.contactInfo)) {
            this.contactInfo = contactInfo;
            updated = true;
        }

        if (updated) {
            System.out.println("Profile updated.");
        } else {
            System.out.println("No changes were made to the profile.");
        }
    }

    public void setPassword(String newPassword) {
        if (newPassword.length() >= 6 &&
                newPassword.matches(".[A-Za-z].") &&
                newPassword.matches(".\\d.")) {
            password = newPassword;
        } else {
            throw new IllegalArgumentException(
                    "Password must be ≥6 chars with letters+numbers.");
        }
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAccessLevel(int level) {
        this.accessLevel = level;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    // للتحقق من كلمة السر الحالية
    public boolean verifyPassword(String passwordToVerify) {
        return this.password.equals(passwordToVerify);
    }

    // ✅ GETTERS NEEDED BY FILEMANAGER AND SYSTEM
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public String getAccessLevelString() {
        switch (this.accessLevel) {
            case 1:
                return "Admin";
            case 2:
                return "Agent";
            case 3:
                return "Customer";
            default:
                return "Unknown";
        }
    }


    // ✅ دالة لعرض القوائم الخاصة بالدور
    public abstract void showMenu();
}
