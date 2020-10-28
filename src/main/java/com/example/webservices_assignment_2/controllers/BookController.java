package com.example.webservices_assignment_2.controllers;

import com.example.webservices_assignment_2.entities.Book;
import com.example.webservices_assignment_2.entities.Game;
import com.example.webservices_assignment_2.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/library/books")
public class BookController {
    private final List<String> supportedExtensions = List.of(".png,.jpg,.jpeg,.gif,.bmp,.jfif".split(","));

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> findAllBooks (@RequestParam(required = false) String title, @RequestParam(required = false) String author,
                                                    @RequestParam(required = false) String genre, @RequestParam(required = false) String ageGroup,
                                                    @RequestParam(required = false) boolean sortOnTitle){
        return ResponseEntity.ok(bookService.findAllBooks(title,author,genre,ageGroup,sortOnTitle));
    }

    @Secured({"ROLE_ADMIN"})
    @PostMapping
    public ResponseEntity<Book> saveNewBook (@Validated @RequestBody Book book){
        return ResponseEntity.ok(bookService.saveNewBook(book));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBook(@PathVariable String id, @RequestBody Book book){
        bookService.updateBook(id,book);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook (@PathVariable String id){
        bookService.deleteBook(id);
    }

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/borrow/{id}")
    public ResponseEntity<Book> borrowBook (@PathVariable String id){
        return ResponseEntity.ok(bookService.borrowBook(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @PutMapping("/return/{id}")
    public ResponseEntity<Book> returnBook (@PathVariable String id) {
        return ResponseEntity.ok(bookService.returnBook(id));
    }

    @Secured("ROLE_ADMIN")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/file/{id}")
    public void uploadBookCover(@RequestParam MultipartFile file,@PathVariable String id) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if (!supportedExtensions.contains(fileExtension.toLowerCase())){
            log.error("You tried to upload an unsupported media type");
            throw new UnsupportedMediaTypeStatusException("File extension invalid");
        }
        bookService.uploadBookCover(file,id);
    }


}
