package com.example.webservices_assignment_2.services;

import com.example.webservices_assignment_2.entities.*;
import com.example.webservices_assignment_2.repositories.MovieRepository;
import com.example.webservices_assignment_2.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "userCache")
    public List<Movie> findAllMovies(String title, String director, String genre, String ageGroup, boolean sortOnTitle){
        log.info("Request made to find all movies.");
        var movieList = movieRepository.findAll();
        if (title != null){
            movieList = movieList.stream().filter(movie -> movie.getTitle().startsWith(title))
                    .collect(Collectors.toList());
        }
        if (director != null){
            movieList = movieList.stream().filter(movie -> movie.getDirector().startsWith(director))
                    .collect(Collectors.toList());
        }
        if (genre != null){
            movieList = movieList.stream().filter(movie -> movie.getGenre().startsWith(genre))
                    .collect(Collectors.toList());
        }
        if (ageGroup != null){
            movieList = movieList.stream().filter(movie -> movie.getAgeGroup().startsWith(ageGroup))
                    .collect(Collectors.toList());
        }
        if (sortOnTitle){
            movieList.sort(Comparator.comparing(Movie::getTitle));
        }
        return movieList;
    }

    @CachePut(value = "userCache",key="#result.movieId")
    public Movie saveNewMovie (Movie movie){
        log.info("New movie saved to database");
        return movieRepository.save(movie);
    }


    @CacheEvict(value = "userCache",key="#id")
    public void updateMovie(String id, Movie movie){

        if (!movieRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find movie with id %s",id));
        }
        movie.setMovieId(id);
        movieRepository.save(movie);
    }

    @CacheEvict(value = "userCache",key = "#id")
    public void deleteMovie (String id){
        if (!movieRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Could not find movie with id %s",id));
        }
        movieRepository.deleteById(id);
    }

    public Movie borrowMovie(String id){
        if (!movieRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find movie with id %s",id));
        }
        if (movieRepository.findById(id).get().isBorrowed()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"This movie has already been borrowed");
        }
        Movie movie = movieRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        movie.setBorrowed(!movie.isBorrowed());
        user.addToListOfLoans(movie);
        movieRepository.save(movie);
        userRepository.save(user);
        return movie;
    }

    public Movie returnMovie (String id) {
        if (!movieRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find movie with id %s",id));
        }
        Movie movie = movieRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (!user.getListOfLoans().contains(movie)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot return a movie you haven't borrowed.");
        }
        user.removeFromListOfLoans(movie);
        movie.setBorrowed(!movie.isBorrowed());
        movieRepository.save(movie);
        userRepository.save(user);
        return movie;
    }

    public void uploadMovieCover(MultipartFile file, String id) throws IOException {
        if (!movieRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find book with id %s",id));
        }
        Movie movie = movieRepository.findById(id).get();
        movie.setImage(new Binary(BsonBinarySubType.BINARY,file.getBytes()));
        movieRepository.save(movie);
    }

}
