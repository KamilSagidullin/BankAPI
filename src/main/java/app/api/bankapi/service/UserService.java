package app.api.bankapi.service;

import app.api.bankapi.entity.User;
import app.api.bankapi.exception.UserNotFoundException;
import app.api.bankapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.REPEATABLE_READ,propagation = Propagation.REQUIRED)

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User save(User user){
        userRepository.save(user);
        return user;
    }
    public void deleteUser(Long id){
        log.info("Deleting user {}", id);
        if (userRepository.existsById(id)){
            userRepository.deleteById(id);
        }
        else {
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }
    }
    public Optional<User> findByFullName(String fullName){
        return userRepository.findByFullName(fullName);
    }
}