package com.example.webservices_assignment_2.controllers;

import com.example.webservices_assignment_2.entities.User;
import com.example.webservices_assignment_2.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Secured({"ROLE_ADMIN","ROLE_USER"})
    @GetMapping
    public ResponseEntity<List<User>> findAll(@RequestParam(required = false) String name, @RequestParam(required = false) boolean sortOnFirstName){
        return ResponseEntity.ok(userService.findAll(name,sortOnFirstName));
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<User> save(@Validated @RequestBody User user){
        return ResponseEntity.ok(userService.save(user));
    }

    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody User user){
        userService.update(id,user);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@PathVariable String id){
        userService.delete(id);
    }
}
