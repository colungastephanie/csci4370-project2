package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.RepostService;
import uga.menik.csx370.services.UserService;

@Controller 
public class RepostController {
    private final UserService userService;
    private final RepostService repostService; 

    @Autowired
    public RepostController(UserService userService, RepostService repostService) {
        this.userService = userService;
        this.repostService = repostService;
    
    }

 @GetMapping("/repost")
     public ModelAndView myReposts( ) {
        
            User user = userService.getLoggedInUser();
            int userId = Integer.parseInt(user.getUserId());

        List <Post> repost = repostService.getRepostedPosts(userId);
        ModelAndView mv = new ModelAndView("repost_page");
        mv.addObject("posts", repost);
        return mv;
    }

    @GetMapping("/post/{postId}/repost/{doRepost}")
    public String toggleRepost(@PathVariable int postId, @PathVariable boolean doRepost
                               ) {
        if (!userService.isAuthenticated()) return "redirect:/login";
        int viewerId = Integer.parseInt(userService.getLoggedInUser().getUserId());
        if (doRepost) {
            repostService.repost(viewerId, postId);
        } else {
            repostService.unrepost(viewerId, postId);
        }
        
        return "redirect:/";
    }

    
   
    
    
}
