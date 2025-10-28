package uga.menik.csx370.models;

import java.util.List;

/**
 * Represents a post in its expanded form within the micro blogging platform.
 * An ExpandedPost includes comments.
 */
public class ExpandedPost extends Post {

    private final List<Comment> comments;

    public ExpandedPost(String postId, String content, String postDate,
                        User user, int heartsCount, int commentsCount,
                        boolean isHearted, boolean isBookmarked,
                        List<Comment> comments) {
        super(postId, content, postDate, user,
              heartsCount, commentsCount, isHearted, isBookmarked,
              0, false); 
        this.comments = comments;
        this.isShowComents = true; 
    }


    public ExpandedPost(String postId, String content, String postDate,
                        User user, int heartsCount, int commentsCount,
                        boolean isHearted, boolean isBookmarked,
                        int repostCount, boolean isReposted,
                        List<Comment> comments) {
        super(postId, content, postDate, user,
              heartsCount, commentsCount, isHearted, isBookmarked,
              repostCount, isReposted); // <-- use the args
        this.comments = comments;
        this.isShowComents = true;
    }

    public List<Comment> getComments() {
        return List.copyOf(comments);
    }
}