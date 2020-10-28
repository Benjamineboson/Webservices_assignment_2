package com.example.webservices_assignment_2.repositories;

import com.example.webservices_assignment_2.entities.Game;
import com.example.webservices_assignment_2.entities.NewsPaper;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game,String> {
}
