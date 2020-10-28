package com.example.webservices_assignment_2.repositories;

import com.example.webservices_assignment_2.entities.Book;
import com.example.webservices_assignment_2.entities.NewsPaper;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book,String> {
}
