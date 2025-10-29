package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.utility.Utility;

@Service
public class BookmarksServices {
    private final DataSource datasource;

    @Autowired
    public BookmarksServices(DataSource dataSource) {
        this.datasource = dataSource;
    }

    public void bookmarkPost(int userId, String postId, Boolean isAdd) throws SQLException {
        try (Connection conn = datasource.getConnection()) {
            if (isAdd == true) {
                try (PreparedStatement ps = conn.prepareStatement(
                    "Insert ignore into bookmark (userId, postId) values (?, ?)")) {
                        ps.setInt(1, userId);
                        ps.setString(2, postId);
                        ps.executeUpdate();
                    };
            } else {
                try(PreparedStatement ps = conn.prepareStatement(
                    "Delete from bookmark where userId = ? and postId = ?")) {
                        ps.setInt(1, userId);
                        ps.setString(2, postId);
                        ps.executeUpdate();
                };
            }
        }
    }

    public List<Post> getAllBookmarks(int userId) {
        List<Post> posts = new ArrayList<>();
    
        final String sql = """
            SELECT
                p.postId, p.content, p.createdAt,
                u.userId, u.firstName, u.lastName,
    
                /* counts */
                (SELECT COUNT(*) FROM likes    l  WHERE l.postId = p.postId) AS likeCount,
                (SELECT COUNT(*) FROM comments c  WHERE c.postId = p.postId) AS commentCount,
                (SELECT COUNT(*) FROM repost   r2 WHERE r2.postId = p.postId) AS repostCount,
    
                /* state flags for this user */
                EXISTS(SELECT 1 FROM likes    l2 WHERE l2.postId = p.postId AND l2.userId = ?) AS isHearted,
                EXISTS(SELECT 1 FROM bookmark b2 WHERE b2.postId = p.postId AND b2.userId = ?) AS isBookmarked,
                EXISTS(SELECT 1 FROM repost   r3 WHERE r3.postId = p.postId AND r3.userId = ?) AS isReposted
    
            FROM bookmark b
            JOIN post  p ON p.postId = b.postId
            JOIN `user` u ON u.userId = p.userId
            WHERE b.userId = ?
            ORDER BY b.createdAt DESC
            """;
    
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
    
                    User author = new User(uid, firstName, lastName);
                    String postDate = Utility.foramtTime(ts); 
    
                    posts.add(new Post(postId, content, postDate, author,
                                       hearts, comments, isHearted, isBookmarked, reposts, isReposted));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error getting bookmarked posts", e);
        }
        return posts;
    }

    public List<Post> getAllReposts(int userId) {
        List<Post> posts = new ArrayList<>();
    
        final String sql = """
            SELECT
                p.postId, p.content, p.createdAt,
                u.userId, u.firstName, u.lastName,
    
                (SELECT COUNT(*) FROM likes    l  WHERE l.postId = p.postId) AS likeCount,
                (SELECT COUNT(*) FROM comments c  WHERE c.postId = p.postId) AS commentCount,
                (SELECT COUNT(*) FROM repost   r2 WHERE r2.postId = p.postId) AS repostCount,
    
                EXISTS(SELECT 1 FROM likes    l2 WHERE l2.postId = p.postId AND l2.userId = ?) AS isHearted,
                EXISTS(SELECT 1 FROM bookmark b2 WHERE b2.postId = p.postId AND b2.userId = ?) AS isBookmarked,
                EXISTS(SELECT 1 FROM repost   r3 WHERE r3.postId = p.postId AND r3.userId = ?) AS isReposted
    
            FROM repost r
            JOIN post  p ON p.postId = r.postId
            JOIN `user` u ON u.userId = p.userId
            WHERE r.userId = ?
            ORDER BY r.createdAt DESC
            """;
    
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
    
                    User author = new User(uid, firstName, lastName);
                    String postDate = Utility.foramtTime(ts);
    
                    posts.add(new Post(postId, content, postDate, author,
                                       hearts, comments, isHearted, isBookmarked, reposts, isReposted));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error getting reposted posts", e);
        }
    
        return posts;
    }
}
