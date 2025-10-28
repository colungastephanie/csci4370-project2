/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.BookmarksServices;
import uga.menik.csx370.services.UserService;

/**
 * Handles /bookmarks and its sub URLs.
 * No other URLs at this point.
 * 
 * Learn more about @Controller here:
 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {
    private final BookmarksServices bookmarksServices;
    private final UserService userService;

    @Autowired
    public BookmarksController(BookmarksServices bookmarksServices, UserService userService) {
        this.bookmarksServices = bookmarksServices;
        this.userService = userService;
    }

    /**
     * /bookmarks URL itself is handled by this.
     */
    @GetMapping
    public ModelAndView webpage() {
        // posts_page is a mustache template from src/main/resources/templates.
        // ModelAndView class enables initializing one and populating placeholders
        // in the template using Java objects assigned to named properties.
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        // Following line populates sample data.
        // You should replace it with actual data from the database.
        ModelAndView mv = new ModelAndView("posts_page");

        try {
            User user = userService.getLoggedInUser();
            int userId = Integer.parseInt(user.getUserId());
            List<Post> bookmarks = bookmarksServices.getAllBookmarks(userId);

            mv.addObject("posts", bookmarks);

            if (bookmarks.isEmpty()) {
                mv.addObject("isNoContent", true);
            }
        } catch (Exception e) {
            mv.addObject("errorMessage", "Failed to load bookmarks.");
        }

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // String errorMessage = "Some error occured!";
        // mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);

        return mv;
    }

    /**
     * /bookmarks/reposts URL - displays all posts reposted by the logged-in user.
     * This is the NEW CUSTOM FEATURE implementation.
     */
    @GetMapping("/reposts")
    public ModelAndView repostsPage() {
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView mv = new ModelAndView("posts_page");

        try {
            User user = userService.getLoggedInUser();
            int userId = Integer.parseInt(user.getUserId());

            // Get all posts that this user has reposted
            List<Post> reposts = bookmarksServices.getAllReposts(userId);

            mv.addObject("posts", reposts);

            if (reposts.isEmpty()) {
                mv.addObject("isNoContent", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "Failed to load reposts.");
            mv.addObject("isNoContent", true);
        }

        return mv;
    }
}
