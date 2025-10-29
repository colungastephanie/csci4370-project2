package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FollowService {
    private final DataSource dataSource;

    @Autowired
    public FollowService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // follow and unfollow
    public void setFollow(String followerId, String followedUserId, boolean isFollow) throws SQLException {
        int follower = Integer.parseInt(followerId);
        int followed = Integer.parseInt(followedUserId);

        try (Connection conn = dataSource.getConnection()) {
            if (isFollow) {
                try (PreparedStatement ps = conn.prepareStatement ("Insert ignore into follow (followerId, followedUserId) values (?, ?)")) {
                    ps.setInt(1, follower);
                    ps.setInt(2, followed);
                    ps.executeUpdate();
                    }
                
            } else {
                try (PreparedStatement ps = conn.prepareStatement("Delete from follow where followerId = ? and followedUserId = ?")) {
                    ps.setInt(1, follower);
                    ps.setInt(2, followed);
                    ps.executeUpdate();
                }
            }
        }
    }
 
}
