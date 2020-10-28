package com.example.webservices_assignment_2.entities;

import lombok.Data;
import org.bson.types.Binary;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.annotation.Id;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.Serializable;

@Data
public class Book implements Serializable {
    @Id
    private String bookId;
    @NotEmpty(message = "Title cannot be empty")
    @Size(max = 40,message = "Book title must not exceed 40 characters")
    private String title;
    @NotEmpty(message = "Author cannot be empty")
    @Size(max = 40,message = "Author must not exceed 40 characters")
    private String author;
    @Size(max = 20,message = "Genre must not exceed 20 characters")
    private String genre;
    @Size(max = 150,message = "Plot must not exceed 150 characters")
    private String plot;
    @NotEmpty(message = "Age group cannot be empty")
    @Size(max = 30,message = "Age group must not exceed 30 characters")
    private String ageGroup;
    private boolean isBorrowed;
    private Binary image;
}
