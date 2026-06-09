package com.kanban.project.security;

import com.kanban.project.data.UserPrincipal;
import com.kanban.project.entity.User;
import com.kanban.project.errors.ExceptionMessage;
import com.kanban.project.errors.UserNotFoundException;
import com.kanban.project.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Service
@AllArgsConstructor
public class UserPrincipalService implements UserDetailsService {
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UserNotFoundException {
        User user =
                userRepository
                        .findUserByEmail(email)
                        .orElseThrow(
                                () -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND));
        return UserPrincipal.fromUser(user);
    }
}