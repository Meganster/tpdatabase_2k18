package forumdb.DAO;


import forumdb.Model.InfoAboutDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


//@Transactional(isolation = Isolation.READ_COMMITTED)
@Repository
public class ServiceDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public InfoAboutDB getStatus() throws DataAccessException {
        final InfoAboutDB currentStatus = new InfoAboutDB();
        currentStatus.setForum(jdbcTemplate.queryForObject("SELECT count(*) FROM Forum;", Long.class));
        currentStatus.setPost(jdbcTemplate.queryForObject("SELECT count(*) FROM Post;", Long.class));
        currentStatus.setThread(jdbcTemplate.queryForObject("SELECT count(*) FROM Thread;", Long.class));
        currentStatus.setUser(jdbcTemplate.queryForObject("SELECT count(*) FROM \"User\";", Long.class));

        return currentStatus;
    }

    public void clear() throws DataAccessException {
        jdbcTemplate.update("TRUNCATE ForumUsers, \"User\", Forum, Thread, Post, UserVoteForThreads CASCADE;");
    }
}
