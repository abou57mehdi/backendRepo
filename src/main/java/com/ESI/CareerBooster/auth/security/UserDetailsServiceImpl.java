package com.ESI.CareerBooster.auth.security;

import com.ESI.CareerBooster.auth.model.User;
import com.ESI.CareerBooster.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getEmail());
        builder.password(user.getPassword());
        builder.roles("USER");
        return builder.build();
    }
} 