package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.utility.Utility;


/**
 * Handles /people URL and its sub URL paths.
 */
@Service
public class RepostService {
    private final DataSource datasource;
    

    @Autowired
    public RepostService(DataSource datasource) {
        this.datasource = datasource;
       
    }

                // Remove repost
    public void unrepost(int userId, int postId) {
                    
        final String sql = "DELETE FROM repost WHERE userId = ? AND postId = ?";
        try (Connection conn = datasource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, userId);
                        ps.setInt(2, postId);
                        ps.executeUpdate();
                    } catch(SQLException e) {
                        throw new RuntimeException("Unrepost Failed", e);
                    }
                } //unrepost
                
            public void repost(int userId, int postId) {
                                
                final String sql = "INSERT INTO repost (userId, postId) VALUES (?, ?)";
                                try (Connection conn = datasource.getConnection();
                                PreparedStatement ps = conn.prepareStatement(sql)) {
                                    ps.setInt(1, userId);
                                    ps.setInt(2, postId);
                                    ps.executeUpdate();
                                } catch(SQLException e) {
                                    throw new RuntimeException("Repost Failed", e);
                                }
                            } //repost 
                    



    // Get all posts that a user has reposted (most recent first)
    public List<Post> getRepostedPosts(int userId) {
        List<Post> posts = new ArrayList<>();
        final String sql = """
                SELECT p.postId, p.content, p.createdAt,
                       u.userId, u.firstName, u.lastName, r.createdAt,

                (SELECT COUNT(*) FROM likes    l  WHERE l.postId = p.postId) AS likeCount,
                (SELECT COUNT(*) FROM comments c  WHERE c.postId = p.postId) AS commentCount,
               
        
                  
                EXISTS(SELECT 1 FROM likes    l2 WHERE l2.postId = p.postId AND l2.userId = ?) AS isHearted,
                EXISTS(SELECT 1 FROM bookmark b2 WHERE b2.postId = p.postId AND b2.userId = ?) AS isBookmarked,
                EXISTS(SELECT 1 FROM repost   r3 WHERE r3.postId = p.postId AND r3.userId = ?) AS isReposted,
                (SELECT COUNT(*) FROM repost    r4 WHERE r4.postId = p.postId) AS repostCount
              
                FROM repost r
                JOIN post p ON p.postId = r.postId
                JOIN user u ON p.userId = u.userId
                WHERE r.userId = ?
                ORDER BY r.createdAt DESC;
                """;

                 List<Post> out = new ArrayList<>();
        try (Connection conn = datasource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
        
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String postId    = rs.getString("postId");
                    String content   = rs.getString("content");
                    java.sql.Timestamp ts = rs.getTimestamp("createdAt");
                    String uid       = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName  = rs.getString("lastName");
    
                    int hearts    = rs.getInt("likeCount");
                    int comments  = rs.getInt("commentCount");
                    int reposts   = rs.getInt("repostCount");
                    boolean isHearted    = rs.getBoolean("isHearted");
                    boolean isBookmarked = rs.getBoolean("isBookmarked");
                    boolean isReposted   = rs.getBoolean("isReposted");
                    String date = Utility.foramtTime(ts); 
                    User user = new User(uid, firstName, lastName);
                    Post post = new Post(postId, content, date, user, hearts, comments, isHearted, isBookmarked, reposts, isReposted);
                     
                        out.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error getting reposted posts", e);
        }
        return posts;
    } //getRepostedPosts 

    /** Count reposts for a post. */
    public int getRepostCount(int postId) {
        final String sql = "SELECT COUNT(*) AS count FROM repost WHERE postId = ?";
        try (Connection conn = datasource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt("count");
            }
        } catch (SQLException e) {
            throw new RuntimeException("error counting reposts for postId=" + postId, e);
        }
        return 0;
    }

    /** Did this user repost this post? */
    public boolean hasUserReposted(int userId, int postId) {
        final String sql = "SELECT 1 FROM repost WHERE userId = ? AND postId = ? LIMIT 1";
        try (Connection conn = datasource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("error checking repost state userId=" + userId + " postId=" + postId, e);
        }
    }

    
} //RepostService 
