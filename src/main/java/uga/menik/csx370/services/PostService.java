package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.utility.Utility;

@Service
public class PostService {
    private final DataSource ds;
    private static final Pattern HASHTAG = Pattern.compile("#(\\w{1,64})");

    @Autowired
    public PostService(DataSource ds) { 
        this.ds = ds; 
    }

    public static List<String> extractTags(String content) {
        List<String> tags = new ArrayList<>();
        Matcher m = HASHTAG.matcher(content);
        while (m.find()) {
            tags.add(m.group(1).toLowerCase(Locale.ROOT));
        }
        return tags;
    }

    public int createPost(int userId, String content) throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                int postId;
                try (PreparedStatement ps = c.prepareStatement(
                    "Insert into post (userId, content) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, userId);
                        ps.setString(2, content);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            keys.next();
                            postId = keys.getInt(1);
                        }
                    }
                List<String> tags = extractTags(content);
                if (!tags.isEmpty()) {
                    try (PreparedStatement insTag = c.prepareStatement(
                        "Insert ignore into hashtag(tag) values (?)");
                        PreparedStatement link = c.prepareStatement(
                            "Insert ignore into post_hashtag(postId, tag) values (?, ?)")) {
                                for (String t : tags) {
                                    insTag.setString(1, t);
                                    insTag.addBatch();

                                    link.setInt(1, postId);
                                    link.setString(2, t);
                                    link.addBatch();
                                }
                                insTag.executeBatch();
                                link.executeBatch();
                            }
                        
                    
                }
                c.commit();
                return postId;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<Post> getAllPosts(Integer userId) {
        List<Post> posts = new ArrayList<>();

        final String sql = """
                SELECT p.postId, p.content, p.createdAt, u.userId, u.firstName, u.lastName
                FROM post p
                JOIN user u ON p.userId = u.userId
                WHERE (? IS NULL OR u.userId IN (
                    SELECT followedUserId FROM follow WHERE followerId = ?
                ))
                ORDER BY p.createdAt DESC
                """;
        try (Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId == null) {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String postId = rs.getString("postId");
                    String content = rs.getString("content");
                    java.sql.Timestamp timeStamp = rs.getTimestamp("createdAt");
                    String useId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");

                    User user = new User(useId, firstName, lastName);
                    String postDate = Utility.foramtTime(timeStamp);
                    Post post = new Post(postId, content, postDate, user, 0, 0, false, false);
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("error receiving posts", e);
        }
        return posts;
    }

}
