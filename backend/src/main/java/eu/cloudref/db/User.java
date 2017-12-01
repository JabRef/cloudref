package eu.cloudref.db;

import eu.cloudref.models.Role;

import javax.persistence.*;
import java.security.Principal;

@Entity
@Table(name = "User")
@Embeddable
public class User implements Principal {

    @Id
    @Column(name = "username")
    private String name;
    @Column(name = "first_name", nullable = false)
    private String firstname;
    @Column(name = "last_name", nullable = false)
    private String lastname;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private Role userRole;
    @Column(name="salt", nullable = false)
    private byte[] salt;

    public User() {
        this.userRole = Role.USER;
    }

    public User(String username, String name, String lastname, String email, String password) {
        this.name = username;
        this.firstname = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.userRole = Role.USER;
    }

    public User(String username, String name, String lastname, String email, String password, Role userRole) {
        this.name = username;
        this.firstname = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
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

    public Role getUserRole() {
        return userRole;
    }

    public void setUserRole(Role userRole) {
        if (userRole != null) {
            this.userRole = userRole;
        }
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}
