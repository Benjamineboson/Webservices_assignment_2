package com.example.webservices_assignment_2.entities;

import lombok.Builder;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class NewsPaper implements Serializable {
    @Id
    private String newsPaperId;
    @NotEmpty(message = "Publisher cannot be empty")
    @Size(max = 40,message = "Newspaper publisher must not exceed 40 characters")
    private String publisher;
    @Size(max = 20,message = "Edition must not exceed 20 characters")
    private String edition;
    @NotEmpty(message = "Language cannot be empty")
    @Size(max = 20,message = "Language must not exceed 40 characters")
    private String language;
    @Size(max = 150,message = "Description must not exceed 150 characters")
    private String description;
    @NotEmpty(message = "Age group cannot be empty")
    @Size(max = 30,message = "Age group must not exceed 30 characters")
    private String ageGroup;
    private boolean isBorrowed;
    private Binary image;
}
