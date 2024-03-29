package me.agno.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeid", nullable = false)
    private Integer employeeID;

    @Nationalized
    @Column(name = "lastname", nullable = false, length = 20)
    private String lastName;

    @Nationalized
    @Column(name = "firstname", nullable = false, length = 10)
    private String firstName;

    @Nationalized
    @Column(name = "title", length = 30)
    private String title;

    @Nationalized
    @Column(name = "titleofcourtesy", length = 25)
    private String titleOfCourtesy;

    @Column(name = "birthdate")
    private LocalDateTime birthDate;

    @Column(name = "hiredate")
    private LocalDateTime hireDate;

    @Nationalized
    @Column(name = "address", length = 60)
    private String address;

    @Nationalized
    @Column(name = "city", length = 15)
    private String city;

    @Nationalized
    @Column(name = "region", length = 15)
    private String region;

    @Nationalized
    @Column(name = "postalcode", length = 10)
    private String postalCode;

    @Nationalized
    @Column(name = "country", length = 15)
    private String country;

    @Nationalized
    @Column(name = "homephone", length = 24)
    private String homePhone;

    @Nationalized
    @Column(name = "extension", length = 4)
    private String extension;

    @Column(name = "photo")
    @JsonIgnore
    private byte[] photo;

    @Transient
    private String base64String;

    public String getBase64String() {
        if (base64String == null || base64String.trim().isEmpty()) {
            base64String = "";
            if (photo != null && photo.length >= 78) {
                var newPhoto = Arrays.copyOfRange(photo, 78, photo.length);
                base64String = Base64.getEncoder().encodeToString(newPhoto);
            }
        }
        return base64String;
    }


    @Nationalized
    @Lob
    @Column(name = "notes")
    private String notes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportsto")
    private Employee reportsTo;

    @Nationalized
    @Column(name = "photopath")
    private String photoPath;

    @JsonIgnore
    @OneToMany(mappedBy = "reportsTo")
    private Set<Employee> employees = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "employeeID")
    private Set<Order> orders = new LinkedHashSet<>();

}