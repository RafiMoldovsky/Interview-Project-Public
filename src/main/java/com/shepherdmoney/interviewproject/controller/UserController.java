package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    // COMPLETE
    private final UserRepository userRepository;
    // use this function to set the userRepository
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        try{
            // TODO: Create an user entity with information given in the payload, store it in the database
            //       and return the id of the user in 200 OK response
            // COMPLETE

            // Creating new user with info from payload
            User user = new User();
            user.setName(payload.getName());
            user.setEmail(payload.getEmail());

            //Save the user in the database
            User savedUser = userRepository.save(user);

            // Return the id of the user in 200 OK response
            return ResponseEntity.ok(savedUser.getId());
        }
        catch (Exception e) {
            // If this does not work, return the proper error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        // COMPLETE

        // First check if a user with this ID exists in the database
        User user = userRepository.findById(userId).orElse(null);
        if(user == null){
            // return 400 bad request 
            return ResponseEntity.badRequest().body("User with ID " + userId + " does not exist");
        }
        
        userRepository.delete(user);

        return ResponseEntity.ok("User with ID " + userId + " has been deleted successfully");
    }
}
