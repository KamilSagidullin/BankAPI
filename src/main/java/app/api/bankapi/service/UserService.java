package app.api.bankapi.service;

import app.api.bankapi.entity.RoleType;
import app.api.bankapi.entity.User;
import app.api.bankapi.exception.UserAlreadyExistsException;
import app.api.bankapi.exception.UserNotFoundException;
import app.api.bankapi.repository.UserRepository;
import app.api.bankapi.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserRoleRepository userRoleRepository;

    public User save(User user){
        if (!userRepository.existsByFullName(user.getFullName())){
            userRepository.save(user);
            for (RoleType roleType : user.getRoles()){
                userRoleRepository.addRoleToUser(user.getId(), roleType.name());
            }
        }
        else {
            log.error("User with this full name already exists");
            throw new UserAlreadyExistsException("User already exists");
        }
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
