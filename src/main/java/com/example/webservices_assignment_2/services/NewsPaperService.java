package com.example.webservices_assignment_2.services;

import com.example.webservices_assignment_2.entities.*;
import com.example.webservices_assignment_2.repositories.NewsPaperRepository;
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

@Slf4j
@Service
public class NewsPaperService {

    @Autowired
    private NewsPaperRepository newsPaperRepository;
    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "userCache")
    public List<NewsPaper> findAllNewsPapers(String publisher,String edition,String language,String ageGroup, boolean sortOnPublisher){
        log.info("Request made to find all newspapers.");
        var newsPaperList = newsPaperRepository.findAll();
        if (publisher != null){
            newsPaperList = newsPaperList.stream().filter(newsPaper -> newsPaper.getPublisher().startsWith(publisher))
                    .collect(Collectors.toList());
        }
        if (edition != null){
            newsPaperList = newsPaperList.stream().filter(newsPaper -> newsPaper.getEdition().startsWith(edition))
                    .collect(Collectors.toList());
        }
        if (language != null){
            newsPaperList = newsPaperList.stream().filter(newsPaper -> newsPaper.getLanguage().startsWith(language))
                    .collect(Collectors.toList());
        }
        if (ageGroup != null){
            newsPaperList = newsPaperList.stream().filter(newsPaper -> newsPaper.getAgeGroup().startsWith(ageGroup))
                    .collect(Collectors.toList());
        }
        if (sortOnPublisher){
            newsPaperList.sort(Comparator.comparing(NewsPaper::getPublisher));
        }
        return newsPaperList;
    }

    @CachePut(value = "userCache",key="#result.newsPaperId")
    public NewsPaper saveNewNewsPaper (NewsPaper newsPaper){
        log.info("New newspaper saved to database");
        return newsPaperRepository.save(newsPaper);
    }

    @CacheEvict(value = "userCache",key="#id")
    public void updateNewsPaper(String id, NewsPaper newsPaper){
        if (!newsPaperRepository.existsById(id)){
            log.error("Could not find the newspaper you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find newspaper with id %s",id));
        }
        newsPaper.setNewsPaperId(id);
        newsPaperRepository.save(newsPaper);
    }

    @CacheEvict(value = "userCache",key = "#id")
    public void deleteNewsPaper (String id){
        if (!newsPaperRepository.existsById(id)){
            log.error("Could not find the newspaper you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Could not find newspaper with id %s",id));
        }
        newsPaperRepository.deleteById(id);
    }

    public NewsPaper borrowNewsPaper(String id){
        if (!newsPaperRepository.existsById(id)){
            log.error("Could not find the newspaper you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find newspaper with id %s",id));
        }
        if (newsPaperRepository.findById(id).get().isBorrowed()){
            log.warn("Attempt made to borrow a newspaper that is unavailable.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"This newspaper has already been borrowed");
        }
        NewsPaper newsPaper = newsPaperRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        newsPaper.setBorrowed(!newsPaper.isBorrowed());
        user.addToListOfLoans(newsPaper);
        newsPaperRepository.save(newsPaper);
        userRepository.save(user);
        return newsPaper;
    }

    public NewsPaper returnNewsPaper (String id) {
        if (!newsPaperRepository.existsById(id)){
            log.error("Could not find the newspaper you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find newspaper with id %s",id));
        }
        NewsPaper newsPaper = newsPaperRepository.findById(id).get();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (!user.getListOfLoans().contains(newsPaper)){
            log.warn("Attempt made to return a newspaper by unauthorized user.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You cannot return a newspaper you haven't borrowed.");
        }
        user.removeFromListOfLoans(newsPaper);
        newsPaper.setBorrowed(!newsPaper.isBorrowed());
        newsPaperRepository.save(newsPaper);
        userRepository.save(user);
        return newsPaper;
    }

    public void uploadNewsPaperCover(MultipartFile file, String id) throws IOException {
        if (!newsPaperRepository.existsById(id)){
            log.error("Could not find the newspaper you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find book with id %s",id));
        }
        NewsPaper newsPaper = newsPaperRepository.findById(id).get();
        newsPaper.setImage(new Binary(BsonBinarySubType.BINARY,file.getBytes()));
        newsPaperRepository.save(newsPaper);
    }

}
