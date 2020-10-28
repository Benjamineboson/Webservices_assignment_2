package com.example.webservices_assignment_2.controllers;

import com.example.webservices_assignment_2.entities.Book;
import com.example.webservices_assignment_2.entities.Game;
import com.example.webservices_assignment_2.entities.Movie;
import com.example.webservices_assignment_2.services.GameService;
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
@RequestMapping("/api/v1/library/games")
public class GameController {
    private final List<String> supportedExtensions = List.of(".png,.jpg,.jpeg,.gif,.bmp,.jfif".split(","));

    @Autowired
    private GameService gameService;

    @GetMapping
    public ResponseEntity<List<Game>> findAllGames (@RequestParam(required = false) String title, @RequestParam(required = false) String genre,
                                                    @RequestParam(required = false) String developer, @RequestParam(required = false) boolean isMultiPlayer,
                                                    @RequestParam(required = false) boolean sortOnTitle){
        return ResponseEntity.ok(gameService.findAllGames(title,genre,developer,isMultiPlayer,sortOnTitle));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<Game> saveNewGame (@Validated @RequestBody Game game){
        return ResponseEntity.ok(gameService.saveNewGame(game));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateGame(@PathVariable String id, @RequestBody Game game){
        gameService.updateGame(id,game);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame (@PathVariable String id){
        gameService.deleteGame(id);
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/borrow/{id}")
    public ResponseEntity<Game> borrowGame (@PathVariable String id){
        return ResponseEntity.ok(gameService.borrowGame(id));
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/return/{id}")
    public ResponseEntity<Game> returnGame (@PathVariable String id) {
        return ResponseEntity.ok(gameService.returnGame(id));
    }

    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/file/{id}")
    public void uploadGameCover(@RequestParam MultipartFile file, @PathVariable String id) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if (!supportedExtensions.contains(fileExtension.toLowerCase())){
            log.error("You tried to upload an unsupported media type");
            throw new UnsupportedMediaTypeStatusException("File extension invalid");
        }
        gameService.uploadGameCover(file,id);
    }
}
