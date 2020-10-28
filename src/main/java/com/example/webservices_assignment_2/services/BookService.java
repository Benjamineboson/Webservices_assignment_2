package com.example.webservices_assignment_2.services;

import com.example.webservices_assignment_2.entities.Book;
import com.example.webservices_assignment_2.entities.User;
import com.example.webservices_assignment_2.repositories.BookRepository;
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
public class BookService {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "userCache")
    public List<Book> findAllBooks(String title, String author, String genre, String ageGroup, boolean sortOnTitle){
        log.info("Request made to find all books.");
        var bookList = bookRepository.findAll();
        if (title != null){
            bookList = bookList.stream().filter(book -> book.getTitle().startsWith(title))
                    .collect(Collectors.toList());
        }
        if (author != null){
            bookList = bookList.stream().filter(book -> book.getAuthor().startsWith(author))
                    .collect(Collectors.toList());
        }
        if (genre != null){
            bookList = bookList.stream().filter(book -> book.getGenre().startsWith(genre))
                    .collect(Collectors.toList());
        }
        if (ageGroup != null){
            bookList = bookList.stream().filter(book -> book.getAgeGroup().startsWith(ageGroup))
                    .collect(Collectors.toList());
        }
        if (sortOnTitle){
            bookList.sort(Comparator.comparing(Book::getTitle));
        }
        return bookList;
    }

    @CachePut(value = "userCache",key="#result.bookId")
    public Book saveNewBook (Book book){
        log.info("New book saved to database");
        return bookRepository.save(book);
    }

    @CacheEvict(value = "userCache",key="#id")
    public void updateBook(String id, Book book){
        if (!bookRepository.existsById(id)){
            log.error("Could not find the book you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find book with id %s",id));
        }
        book.setBookId(id);
        bookRepository.save(book);
    }

    @CacheEvict(value = "userCache",key = "#id")
    public void deleteBook (String id){
        if (!bookRepository.existsById(id)){
            log.error("Could not find the book you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Could not find book with id %s",id));
        }
        bookRepository.deleteById(id);
    }

    public Book borrowBook(String id){
        if (!bookRepository.existsById(id)){
            log.error("Could not find the book you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find book with id %s",id));
        }
        if (bookRepository.findById(id).get().isBorrowed()){
            log.warn("Attempt made to borrow a book that is unavailable.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"This book has already been borrowed");
        }
        Book book = bookRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        book.setBorrowed(!book.isBorrowed());
        user.addToListOfLoans(book);
        bookRepository.save(book);
        userRepository.save(user);
        return book;
    }

    public Book returnBook (String id) {
        if (!bookRepository.existsById(id)){
            log.error("Could not find the book you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find book with id %s",id));
        }
        Book book = bookRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (!user.getListOfLoans().contains(book)){
            log.warn("Attempt made to return a book by unauthorized user.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot return a book you haven't borrowed.");
        }
        user.removeFromListOfLoans(book);
        book.setBorrowed(!book.isBorrowed());
        bookRepository.save(book);
        userRepository.save(user);
        return book;
    }

    public void uploadBookCover(MultipartFile file, String id) throws IOException {
        if (!bookRepository.existsById(id)){
            log.error("Could not find the book you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find book with id %s",id));
        }
        Book book = bookRepository.findById(id).get();
        book.setImage(new Binary(BsonBinarySubType.BINARY,file.getBytes()));
        bookRepository.save(book);
    }
}
