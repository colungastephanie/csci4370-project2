/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


import javax.sql.DataSource;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.UserService;
// import uga.menik.csx370.utility.Utility;
/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    // UserService has user login and registration related functions.
    private final UserService userService;
    private final DataSource dataSource; 

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public ProfileController(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource; 
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public ModelAndView profileOfLoggedInUser() {
        System.out.println("User is attempting to view profile of the logged in user.");
        return profileOfSpecificUser(userService.getLoggedInUser().getUserId());
    }

    /**
     * This function handles /profile/{userId} URL.
     * This serves the webpage that shows posts of a speific user given by userId.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * how path variables work.
     */
    @GetMapping("/{userId}")
    public ModelAndView profileOfSpecificUser(@PathVariable("userId") String userId) {
        System.out.println("User is attempting to view profile: " + userId);
        
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        try {
        List<Post> posts = getUserPosts(userId);
        mv.addObject("posts", posts);
        } catch (Exception e) {
        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
         String errorMessage = "Some error occured!";
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
         mv.addObject("isNoContent", true);
        } //catch 
        return mv;
    }

    private List<Post> getUserPosts(String userId) throws SQLException {
        final String sql = "select P.postId, P.content, P.createdAt, U.userId, U.firstName, U.lastName " +
        "from post P join `user` U on P.userId = U.userId " +
        "where P.userId = ? " + 
        "order by P.createdAt desc";

        List<Post> out = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1,Integer.parseInt(userId));

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        User user = new User (
                            String.valueOf(rs.getInt("userId")),
                            rs.getString("firstName"),
                            rs.getString("lastName")
                        ); 
                        String date = dtf.format(
                            rs.getTimestamp("createdAt").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        );
                        Post post = new Post (
                            String.valueOf(rs.getInt("postId")),
                            rs.getString("content"),
                            date, 
                            user, 
                            0, 0, false, false
                        );
                        out.add(post);
                    } //while
                } //try - try
            } //try
            return out;
    } //getUserPosts
    
}
