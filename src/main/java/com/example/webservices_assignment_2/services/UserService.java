package com.example.webservices_assignment_2.services;

import com.example.webservices_assignment_2.entities.User;
import com.example.webservices_assignment_2.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Cacheable(value = "userCache")
    public List<User> findAll(String name,boolean sortOnFirstName){
        log.info("Request made to find all users");
        var userList = userRepository.findAll();
        if (name != null){
            userList = userList.stream().filter(user -> user.getFirstName().startsWith(name))
                    .collect(Collectors.toList());
        }
        if (sortOnFirstName){
            userList.sort(Comparator.comparing(User::getFirstName));
        }
        return userList;
    }

    @Cacheable(value = "userCache",key = "#username")
    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Could not find the user with username %s",username)));
    }

    @CachePut(value = "userCache",key="#result.userId")
    public User save(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @CacheEvict(value = "userCache",key = "#id")
    public void delete (String id){
        log.info(String.format("Request made to delete user with id %s",id));
        if (!userRepository.existsById(id)){
            log.error("Couldn't find the user you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Could not find user with id %s",id));
        }
        userRepository.deleteById(id);
    }

    @CacheEvict(value = "userCache",key="#id")
    public void update(String id, User user){
        var isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().toUpperCase().equals("ROLE_ADMIN"));
        var isCurrentUser = SecurityContextHolder.getContext().getAuthentication()
                .getName().toLowerCase().equals(user.getUsername().toLowerCase());
        if (!isAdmin && !isCurrentUser){
            log.warn("Attempt to update another user made.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You can only update yourself");
        }
        if (!userRepository.existsById(id)){
            log.error("Couldn't find the user you were looking for.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,String.format("Could not find user with id %s",id));
        }
        if (user.getPassword().length() <= 16){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setUserId(id);
        userRepository.save(user);
    }

}
