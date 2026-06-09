package com.kanban.project.service;

import com.kanban.project.dto.LoginRequestDto;
import com.kanban.project.dto.LoginResponseDto;
import com.kanban.project.dto.UserDto;
import com.kanban.project.dto.UserRegistrationDto;
import com.kanban.project.entity.User;
import com.kanban.project.errors.ExceptionMessage;
import com.kanban.project.errors.UserAlreadyExistsException;
import com.kanban.project.errors.UserNotFoundException;
import com.kanban.project.helper.JwtHelper;
import com.kanban.project.mapper.UserMapper;
import com.kanban.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional
    public void register(UserRegistrationDto userRegistrationDto) {
        String userEmail = userRegistrationDto.email();
        if (userRepository.existsByEmail(userEmail)) {
            throw new UserAlreadyExistsException(ExceptionMessage.USER_ALREADY_EXISTS);
        }

        User user = userMapper.toEntity(userRegistrationDto);
        user.setPasswordHash(passwordEncoder.encode(userRegistrationDto.password()));
        userRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));

        User user = userRepository.findUserByEmail(dto.email())
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND));

        return new LoginResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                jwtHelper.generateToken(user));
    }

    public List<UserDto> searchUsers(String query, Long excludeId) {
        List<User> users = userRepository.findByEmailContainingAndIdNot(query, excludeId);

        return users.stream()
                .map(user ->
                        new UserDto(user.getId(), user.getNickname(), user.getEmail()))
                .toList();
    }

    public List<UserDto> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids)
                .stream()
                .map(user ->
                        new UserDto(user.getId(), user.getNickname(), user.getEmail()))
                .toList();
    }
}
