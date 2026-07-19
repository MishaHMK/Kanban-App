package com.kanban.project.security;

import com.kanban.project.data.UserPrincipal;
import com.kanban.project.entity.User;
import com.kanban.project.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
@AllArgsConstructor
public class UserPrincipalService implements UserDetailsService {
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user =
                userRepository
                        .findUserByEmail(email)
                        .orElseThrow(
                                () -> new UsernameNotFoundException(email));
        return UserPrincipal.fromUser(user);
    }
}