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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
 
import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.models.User;
import uga.menik.csx370.utility.Utility;
import uga.menik.csx370.services.PostService; 
import uga.menik.csx370.services.BookmarksServices;

/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final BookmarksServices bookmarksServices;

    @Autowired
    public PostController(PostService postService, UserService userService, BookmarksServices bookmarksServices) {
        this.postService = postService;
        this.userService = userService;
        this.bookmarksServices = bookmarksServices;
    }

    public ModelAndView composePage(@RequestParam(name = "error", required = false) String error) {
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView mv = new ModelAndView("post_new");
        mv.addObject("errorMessage", error);
        return mv;
    }

    @PostMapping
    public String createPost(@RequestParam("content") String content) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            int userId = Integer.parseInt(userService.getLoggedInUser().getUserId());
            postService.createPost(userId, content);
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            String m = URLEncoder.encode("Failed to create post.", StandardCharsets.UTF_8);
            return "redirect:/post/new?error=" + m;
        }
    }
    /**
     * This function handles the /post/{postId} URL.
     * This handlers serves the web page for a specific post.
     * Note there is a path variable {postId}.
     * An example URL handled by this function looks like below:
     * http://localhost:8081/post/1
     * The above URL assigns 1 to postId.
     * 
     * See notes from HomeController.java regardig error URL parameter.
     */
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error) {
        System.out.println("The user is attempting to view post with id: " + postId);
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

<<<<<<< Updated upstream
        // Following line populates sample data.
        // You should replace it with actual data from the database.
        List<ExpandedPost> posts = Utility.createSampleExpandedPostWithComments();
        mv.addObject("posts", posts);
=======
        try {
            int postIdInt = Integer.parseInt(postId);

            // Get the post with all details from database
            String sql = "SELECT p.postId, p.userId, p.content, p.createdAt, " +
                    "u.username, u.firstName, u.lastName " +
                    "FROM post p " +
                    "JOIN user u ON p.userId = u.userId " +
                    "WHERE p.postId = ?";

            List<ExpandedPost> posts = jdbcTemplate.query(sql, (rs, rowNum) -> {
                // Create User object
                User user = new User(
                        String.valueOf(rs.getInt("userId")),
                        rs.getString("username"),
                        rs.getString("firstName"),
                        rs.getString("lastName"));

                // Get comments for this post (ordered oldest to latest)
                List<Comment> comments = postService.getCommentsByPostId(rs.getInt("postId"));

                // Get counts and user interaction status
                int likesCount = postService.getLikesCount(rs.getInt("postId"));
                int commentCount = postService.getCommentCount(rs.getInt("postId"));
                boolean isLiked = (userId != null) ? postService.hasUserLikedPost(userId, rs.getInt("postId")) : false;
                boolean isBookmarked = (userId != null) ? postService.hasUserBookmarked(userId, rs.getInt("postId"))
                        : false;

                // Create ExpandedPost using the constructor
                return new ExpandedPost(
                        String.valueOf(rs.getInt("postId")),
                        rs.getString("content"),
                        rs.getTimestamp("createdAt").toString(),
                        user,
                        likesCount,
                        commentCount,
                        isLiked,
                        isBookmarked,
                        postService.getRepostCount(rs.getInt("postId")),
                        postService.hasUserReposted(userId, rs.getInt("postId")),
                        comments);
            }, postIdInt);

            if (posts.isEmpty()) {
                mv.addObject("isNoContent", true);
                mv.addObject("errorMessage", "Post not found");
            } else {
                mv.addObject("posts", posts);
            }

        } catch (Exception e) {
            System.err.println("Error loading post: " + e.getMessage());
            e.printStackTrace();
            mv.addObject("isNoContent", true);
            mv.addObject("errorMessage", "Failed to load post");
        }
>>>>>>> Stashed changes

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);

        return mv;
    }

    /**
     * Handles comments added on posts.
     * See comments on webpage function to see how path variables work here.
     * This function handles form posts.
     * See comments in HomeController.java regarding form submissions.
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
            @RequestParam(name = "comment") String comment) {
        System.out.println("The user is attempting add a comment:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tcomment: " + comment);

        // Redirect the user if the comment adding is a success.
        // return "redirect:/post/" + postId;

        // Redirect the user with an error message if there was an error.
        String message = URLEncoder.encode("Failed to post the comment. Please try again.",
                StandardCharsets.UTF_8);
        return "redirect:/post/" + postId + "?error=" + message;
    }

    /**
     * Handles likes added on posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions and how path variables work.
     */
    @GetMapping("/{postId}/heart/{isAdd}")
    public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a heart:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        // Redirect the user if the comment adding is a success.
        // return "redirect:/post/" + postId;

        // Redirect the user with an error message if there was an error.
        String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
                StandardCharsets.UTF_8);
        return "redirect:/post/" + postId + "?error=" + message;
    }

    /**
     * Handles bookmarking posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions.
     */
    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        // Redirect the user if the comment adding is a success.
        // return "redirect:/post/" + postId;
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User user = userService.getLoggedInUser();
            int userId = Integer.parseInt(user.getUserId());

            bookmarksServices.bookmarkPost(userId, postId, isAdd);
            return "redirect:/bookmarks";
        } catch (Exception e) {
        // Redirect the user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + "?error=" + message;
        }
    }

}
