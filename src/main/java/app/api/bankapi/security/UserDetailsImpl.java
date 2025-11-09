package app.api.bankapi.security;

import app.api.bankapi.exception.UserNotFoundException;
import app.api.bankapi.repository.UserRepository;
import app.api.bankapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByFullName(username).map(MyUserDetails::new).orElseThrow(() -> new UserNotFoundException("User was not found"));

    }
}
