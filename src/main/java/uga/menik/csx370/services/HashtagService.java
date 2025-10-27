package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.utility.Utility;

@Service
public class HashtagService {
    private final DataSource dataSource;

    @Autowired
    public HashtagService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Post> getPostByHashtag(List<String> tags) {
        if (tags.isEmpty()) {
            return new ArrayList<>();
        }

        List<Post> posts = new ArrayList<>();
        String[] arr = new String[tags.size()];
        Arrays.fill(arr, "?");
        String placeholders = String.join(",", arr);
        
        final String sql = String.format("""
                SELECT p.postId, p.content, p.createdAt, u.userId, u.firstName, u.lastName
                FROM post p
                JOIN user u ON p.userId = u.userId
                JOIN post_hashtag ph ON p.postId = ph.postId 
                WHERE ph.tag IN (%s)
                GROUP BY p.postId
                HAVING COUNT(DISTINCT ph.tag) = ?
                ORDER BY p.createdAt DESC
                """, placeholders);

        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
                int i = 1;
                for (String tag : tags) {
                ps.setString(i++, tag.toLowerCase());
            }
                ps.setInt(i, tags.size());
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