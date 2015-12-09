package bonvio.configs;

import bonvio.model.Repo.RoleRepo;
import bonvio.model.Repo.UserRepo;

import bonvio.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by alexggg99 on 08.12.15.
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        bonvio.model.User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
        }

        List<GrantedAuthority> authorities = buildUserAuthority(user.authority);

        return buildUserForAuthentication(user, authorities);
    }


    public User buildUserForAuthentication(bonvio.model.User user,
                                           List<GrantedAuthority> authorities) {
        return new User(user.username, user.password,
                true, true, true, true, authorities);
    }

    private List<GrantedAuthority> buildUserAuthority(
            Role role) {

        Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();


        //for (int i = 0; i < roles.size(); i ++){

            //String role = roles.get(i).getAuthority(); //TODO Превратить в строку типа "ROLE_ADMIN"

            setAuths.add(new SimpleGrantedAuthority(role.getAuthority()));


        //}

        List<GrantedAuthority> result = new ArrayList<GrantedAuthority>(
                setAuths);

        return result;
    }

    public UserDetails getUser(bonvio.model.User user ){
        List<GrantedAuthority> authorities = buildUserAuthority(user.authority);

        return buildUserForAuthentication(user, authorities);
    }



}
