package bonvio.model.Repo;


import bonvio.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by alexggg99 on 04.12.15.
 */

@Transactional
public interface UserRepo extends CrudRepository<User, Long> {

        public User findByIdVk(long idVk);

        public User findByUsername(String username);

}