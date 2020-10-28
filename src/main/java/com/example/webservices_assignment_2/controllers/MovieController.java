package com.example.webservices_assignment_2.controllers;

import com.example.webservices_assignment_2.entities.Book;
import com.example.webservices_assignment_2.entities.Movie;
import com.example.webservices_assignment_2.entities.NewsPaper;
import com.example.webservices_assignment_2.services.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/library/movies")
public class MovieController {
    private final List<String> supportedExtensions = List.of(".png,.jpg,.jpeg,.gif,.bmp,.jfif".split(","));

    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<List<Movie>> findAllMovies (@RequestParam(required = false) String title, @RequestParam(required = false) String director,
                                                      @RequestParam(required = false) String genre, @RequestParam(required = false) String ageGroup,
                                                      @RequestParam(required = false) boolean sortOnTitle){
        return ResponseEntity.ok(movieService.findAllMovies(title,director,genre,ageGroup,sortOnTitle));
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping
    public ResponseEntity<Movie> saveNewMovie (@Validated @RequestBody Movie movie){
        return ResponseEntity.ok(movieService.saveNewMovie(movie));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMovie(@PathVariable String id, @RequestBody Movie movie){
        movieService.updateMovie(id,movie);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie (@PathVariable String id){
        movieService.deleteMovie(id);
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/borrow/{id}")
    public ResponseEntity<Movie> borrowMovie (@PathVariable String id){
        return ResponseEntity.ok(movieService.borrowMovie(id));
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/return/{id}")
    public ResponseEntity<Movie> returnMovie (@PathVariable String id) {
        return ResponseEntity.ok(movieService.returnMovie(id));
    }

    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/file/{id}")
    public void uploadMovieCover(@RequestParam MultipartFile file, @PathVariable String id) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if (!supportedExtensions.contains(fileExtension.toLowerCase())){
            log.error("You tried to upload an unsupported media type");
            throw new UnsupportedMediaTypeStatusException("File extension invalid");
        }
        movieService.uploadMovieCover(file,id);
    }
}
