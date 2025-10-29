/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.
*/
package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Comment;
import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;
@Controller
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostController(PostService postService,
            UserService userService,
            JdbcTemplate jdbcTemplate) {
        this.postService = postService;
        this.userService = userService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/new")
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

    // GET /post/{postId} — single post page with comments
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("posts_page");

        Integer currentUserId = userService.isAuthenticated()
                ? Integer.valueOf(userService.getLoggedInUser().getUserId())
                : null;

        try {
            int pid = Integer.parseInt(postId);

            final String sql = """
                        SELECT p.postId, p.content, p.createdAt,
                               u.userId, u.username, u.firstName, u.lastName
                        FROM post p
                        JOIN user u ON u.userId = p.userId
                        WHERE p.postId = ?
                    """;

            RowMapper<User> userMapper = (rs, rowNum) -> new User(
                    String.valueOf(rs.getInt("userId")),
                    rs.getString("firstName"),
                    rs.getString("lastName"));

            List<ExpandedPost> onePost = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Timestamp ts = rs.getTimestamp("createdAt");
                LocalDateTime ldt = ts.toLocalDateTime();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

                List<Comment> comments = postService.getCommentsByPostId(rs.getInt("postId"));

                int likesCount = postService.getLikesCount(rs.getInt("postId"));
                int commentCount = postService.getCommentCount(rs.getInt("postId"));
                boolean isLiked = (currentUserId != null)
                        && postService.hasUserLikedPost(currentUserId, rs.getInt("postId"));
                boolean isBookmarked = (currentUserId != null)
                        && postService.hasUserBookmarked(currentUserId, rs.getInt("postId"));

                return new ExpandedPost(
                        String.valueOf(rs.getInt("postId")),
                        rs.getString("content"),
                        ldt.format(fmt),
                        userMapper.mapRow(rs, rowNum),
                        likesCount,
                        commentCount,
                        isLiked,
                        isBookmarked,
                        comments);
            }, pid);

            if (onePost.isEmpty()) {
                mv.addObject("isNoContent", true);
            } else {
                mv.addObject("posts", onePost);
            }

            mv.addObject("errorMessage", error);
            return mv;

        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("isNoContent", true);
            String m = URLEncoder.encode("Could not load the post.", StandardCharsets.UTF_8);
            mv.addObject("errorMessage", m);
            return mv;
        }
    }

    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
            @RequestParam(name = "comment") String comment) {
        if (!userService.isAuthenticated()) {
            String message = URLEncoder.encode("You must be logged in to comment.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }

        try {
            int uid = Integer.parseInt(userService.getLoggedInUser().getUserId());
            int pid = Integer.parseInt(postId);
            String trimmed = comment == null ? "" : comment.trim();
            if (trimmed.isEmpty()) {
                String message = URLEncoder.encode("Comment cannot be empty.", StandardCharsets.UTF_8);
                return "redirect:/post/" + postId + "?error=" + message;
            }
            postService.addComment(uid, pid, trimmed);
            return "redirect:/post/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to post the comment. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    @GetMapping("/{postId}/heart/{isAdd}")
    public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            User user = userService.getLoggedInUser();
            int userId = Integer.parseInt(user.getUserId());
            int pid = Integer.parseInt(postId);

            // Toggle the like
            postService.toggleLike(userId, pid);

            return "redirect:/post/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            User user = userService.getLoggedInUser();
            int userId = Integer.parseInt(user.getUserId());
            int pid = Integer.parseInt(postId);

            // Toggle the bookmark
            postService.toggleBookmark(userId, pid);

            return "redirect:/post/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    /* @GetMapping("/{postId}/repost/{isAdd}")
    public String addOrRemoveRepost(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            User user = userService.getLoggedInUser();
            int userId = Integer.parseInt(user.getUserId());
            int pid = Integer.parseInt(postId);

            // Toggle the repost
            repostService.toggleRepost(userId, pid);

            return "redirect:/post/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)repost the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    } */
}