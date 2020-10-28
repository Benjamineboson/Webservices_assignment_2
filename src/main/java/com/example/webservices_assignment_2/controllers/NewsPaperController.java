package com.example.webservices_assignment_2.controllers;

import com.example.webservices_assignment_2.entities.Game;
import com.example.webservices_assignment_2.entities.NewsPaper;
import com.example.webservices_assignment_2.entities.User;
import com.example.webservices_assignment_2.services.NewsPaperService;
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
@RequestMapping("/api/v1/library/newsPapers")
public class NewsPaperController {
    private final List<String> supportedExtensions = List.of(".png,.jpg,.jpeg,.gif,.bmp,.jfif".split(","));

    @Autowired
    private NewsPaperService newsPaperService;

    @GetMapping
    public ResponseEntity<List<NewsPaper>> findAllNewsPapers (@RequestParam(required = false) String publisher, @RequestParam(required = false) String edition,
                                                              @RequestParam(required = false) String language, @RequestParam(required = false) String ageGroup,
                                                              @RequestParam(required = false) boolean sortOnPublisher){
        return ResponseEntity.ok(newsPaperService.findAllNewsPapers(publisher,edition,language,ageGroup,sortOnPublisher));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<NewsPaper> saveNewNewsPaper (@Validated @RequestBody NewsPaper newsPaper){
        return ResponseEntity.ok(newsPaperService.saveNewNewsPaper(newsPaper));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNewsPaper(@PathVariable String id, @RequestBody NewsPaper newsPaper){
        newsPaperService.updateNewsPaper(id,newsPaper);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNewspaper (@PathVariable String id){
        newsPaperService.deleteNewsPaper(id);
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/borrow/{id}")
    public ResponseEntity<NewsPaper> borrowNewsPaper (@PathVariable String id){
        return ResponseEntity.ok(newsPaperService.borrowNewsPaper(id));
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/return/{id}")
    public ResponseEntity<NewsPaper> returnNewsPaper (@PathVariable String id) {
        return ResponseEntity.ok(newsPaperService.returnNewsPaper(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/file/{id}")
    public void uploadNewsPaperCover(@RequestParam MultipartFile file, @PathVariable String id) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if (!supportedExtensions.contains(fileExtension.toLowerCase())){
            log.error("You tried to upload an unsupported media type");
            throw new UnsupportedMediaTypeStatusException("File extension invalid");
        }
        newsPaperService.uploadNewsPaperCover(file,id);
    }
}
