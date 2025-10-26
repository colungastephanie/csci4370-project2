/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;
import uga.menik.csx370.models.FollowableUser;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.FollowService;
import uga.menik.csx370.services.PeopleService;
import uga.menik.csx370.services.UserService;

/**
 * Handles /people URL and its sub URL paths.
 */
@Controller
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;
    private final UserService userService;
    private final FollowService followService;

    @Autowired
    public PeopleController(PeopleService peopleService, UserService userService, FollowService followService) { // <- only TWO params
        this.peopleService = peopleService;
        this.userService = userService;
        this.followService = followService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        // require login (optional but recommended)
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }

        // correct ctor: just pass the view name string
        ModelAndView mv = new ModelAndView("people_page");

        // get current viewer id safely
        User viewer = userService.getLoggedInUser();
        String viewerId = (viewer != null) ? viewer.getUserId() : null;
        try {
        // call the service (do NOT reference Utility or a static field)
        List<FollowableUser> followableUsers = peopleService.getFollowableUsers(viewerId);
        mv.addObject("users", followableUsers); // correct addObject signature
          if (followableUsers.isEmpty()) {
            mv.addObject("isNoContent", true);
        }
    } catch (Exception e) {
        
        // optional error message passthrough
        mv.addObject("errorMessage", e);
    }   
        return mv;
    }

    /**
     * This function handles user follow and unfollow.
     * Note the URL has parameters defined as variables ie: {userId} and {isFollow}.
     * Follow and unfollow is handled by submitting a get type form to this URL 
     * by specifing the userId and the isFollow variables.
     * Learn more here: https://www.w3schools.com/tags/att_form_method.asp
     * An example URL that is handled by this function looks like below:
     * http://localhost:8081/people/1/follow/false
     * The above URL assigns 1 to userId and false to isFollow.
     */
    @GetMapping("{userId}/follow/{isFollow}")
    public String followUnfollowUser(@PathVariable("userId") String userId,
            @PathVariable("isFollow") Boolean isFollow) {
        System.out.println("User is attempting to follow/unfollow a user:");
        System.out.println("\tuserId: " + userId);
        System.out.println("\tisFollow: " + isFollow);
        

        // Redirect the user if the comment adding is a success.
        // return "redirect:/people";
        if (!userService.isAuthenticated()) {
            return "redirect: /login";
        }
        String viewerId = userService.getLoggedInUser().getUserId();
        if (viewerId == null || viewerId.equals(userId)) {
            return "redirect:/people";
        }
        try {
           
                followService.setFollow(viewerId, userId, Boolean.TRUE.equals(isFollow));
             
              return "redirect:/people";
        } catch (Exception e) {
            // Redirect the user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to (un)follow the user. Please try again.",
            StandardCharsets.UTF_8);
            return "redirect:/people?error=" + message;
        }
    
    }

}
