package com.example.webservices_assignment_2.entities;

import lombok.Builder;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class Game implements Serializable {
    @Id
    private String gameId;
    @NotEmpty(message = "Title cannot be empty.")
    @Size(max = 40,message = "Game title must not exceed 40 characters.")
    private String title;
    @Size(max = 20,message = "Genre must not exceed 20 characters.")
    private String genre;
    @NotEmpty(message = "Developer cannot be empty")
    @Size(max = 40,message = "Developer must not exceed 40 characters.")
    private String developer;
    private boolean isMultiPlayer;
    @NotEmpty(message = "Age group cannot be empty")
    @Size(max = 30,message = "Age group must not exceed 30 characters")
    private String ageGroup;
    private boolean isBorrowed;
    private Binary image;
}
