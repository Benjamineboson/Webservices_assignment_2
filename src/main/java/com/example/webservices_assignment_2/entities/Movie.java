package com.example.webservices_assignment_2.entities;

import lombok.Builder;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class Movie implements Serializable {
    @Id
    private String movieId;
    @NotEmpty(message = "Title cannot be empty")
    @Size(max = 40,message = "Movie title must not exceed 40 characters")
    private String title;
    @NotEmpty(message = "Director cannot be empty")
    @Size(max = 40,message = "Director must not exceed 40 characters")
    private String director;
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
