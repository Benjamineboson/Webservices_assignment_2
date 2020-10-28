package com.example.webservices_assignment_2.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class User implements Serializable {
    private static final long serialVersionUID = 1l;
    @Id
    private String userId;
    @NotEmpty(message = "First name cannot be empty")
    @Size(min = 2,max = 25,message = "First name must be between 3-10 characters")
    private String firstName;
    @NotEmpty(message = "Last name cannot be empty")
    @Size(min = 2,max = 25,message = "First name must be between 3-10 characters")
    private String lastName;
    @Indexed(unique = true)
    @Email(message = "Email not valid.")
    private String email;
    @Indexed(unique = true)
    @Size(min = 4,max = 10,message = "Username not valid")
    private String username;
    @Size(min = 4,max = 16,message = "Password not valid")
    @NotBlank(message = "Password cannot be empty")
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Pattern(regexp = "([0-9]){2,4}-([0-9]){5,8}",message = "Phone number not valid")
    private String phoneNumber;
    @Past(message = "Date can't be in the future.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd",shape = JsonFormat.Shape.STRING)
    private LocalDate birthDate;
    private List<String> acl;
    @NotNull
    private List<Object> listOfLoans;

    public void addToListOfLoans (Object object){
        listOfLoans.add(object);
    }

    public void removeFromListOfLoans (Object object){
        listOfLoans.remove(object);
    }
}
