package com.example.webservices_assignment_2.services;

import com.example.webservices_assignment_2.entities.Game;
import com.example.webservices_assignment_2.entities.User;
import com.example.webservices_assignment_2.repositories.GameRepository;
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
public class GameService {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "userCache")
    public List<Game> findAllGames(String title,String genre,String developer, boolean isMultiPlayer, boolean sortOnTitle){
        log.info("Request made to find all games.");
        var gameList = gameRepository.findAll();
        if (title != null){
            gameList = gameList.stream().filter(game -> game.getTitle().startsWith(title))
                    .collect(Collectors.toList());
        }
        if (genre != null){
            gameList = gameList.stream().filter(game -> game.getGenre().startsWith(genre))
                    .collect(Collectors.toList());
        }
        if (developer != null){
            gameList = gameList.stream().filter(game -> game.getDeveloper().startsWith(developer))
                    .collect(Collectors.toList());
        }
        if (isMultiPlayer){
            gameList = gameList.stream().filter(game -> game.isMultiPlayer())
                    .collect(Collectors.toList());
        }
        if (sortOnTitle){
            gameList.sort(Comparator.comparing(Game::getTitle));
        }
        return gameList;
    }

    @CachePut(value = "userCache",key="#result.gameId")
    public Game saveNewGame (Game game){
        log.info("New game saved to database");
        return gameRepository.save(game);
    }

    @CacheEvict(value = "userCache",key="#id")
    public void updateGame(String id, Game game){
        if (!gameRepository.existsById(id)){
            log.error("Could not find the game you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find game with id %s",id));
        }
        game.setGameId(id);
        gameRepository.save(game);
    }

    @CacheEvict(value = "userCache",key = "#id")
    public void deleteGame (String id){
        if (!gameRepository.existsById(id)){
            log.error("Could not find the game you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Could not find game with id %s",id));
        }
        gameRepository.deleteById(id);
    }

    public Game borrowGame(String id){
        if (!gameRepository.existsById(id)){
            log.error("Could not find the game you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find game with id %s",id));
        }
        if (gameRepository.findById(id).get().isBorrowed()){
            log.warn("Attempt made to borrow a game that is unavailable.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"This game has already been borrowed");
        }
        Game game = gameRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        game.setBorrowed(!game.isBorrowed());
        user.addToListOfLoans(game);
        gameRepository.save(game);
        userRepository.save(user);
        return game;
    }

    public Game returnGame (String id) {
        if (!gameRepository.existsById(id)){
            log.error("Could not find the game you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find game with id %s",id));
        }
        Game game = gameRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (!user.getListOfLoans().contains(game)){
            log.warn("Attempt made to return a game by unauthorized user.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot return a game you haven't borrowed.");
        }
        user.removeFromListOfLoans(game);
        game.setBorrowed(!game.isBorrowed());
        gameRepository.save(game);
        userRepository.save(user);
        return game;
    }

    public void uploadGameCover(MultipartFile file, String id) throws IOException {
        if (!gameRepository.existsById(id)){
            log.error("Could not find the game you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find book with id %s",id));
        }
        Game game = gameRepository.findById(id).get();
        game.setImage(new Binary(BsonBinarySubType.BINARY,file.getBytes()));
        gameRepository.save(game);
    }
}
