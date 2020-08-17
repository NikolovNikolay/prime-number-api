package com.nikolaynikolov.primenumberapi.service;

import com.nikolaynikolov.primenumberapi.model.User;
import com.nikolaynikolov.primenumberapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;
  private final CacheService cacheService;

  @Autowired
  public UserServiceImpl(UserRepository userRepository, CacheService cacheService) {
    this.userRepository = userRepository;
    this.cacheService = cacheService;
  }

  public Optional<User> getUserByKey(String key) {
    User u = cacheService.getUser(key);
    if (u != null) {
      return Optional.of(u);
    }
    var rawUser = this.userRepository.findFirstByKey(key);
    rawUser.ifPresent(user -> cacheService.setUser(key, user));
    return rawUser;
  }

  public Set<GrantedAuthority> getAuthorities(User user) {
    return user.getPermissions()
        .stream()
        .map(p -> new SimpleGrantedAuthority(p.getName()))
        .collect(Collectors.toSet());
  }

  @Override
  public UserDetails loadUserByUsername(String key) throws UsernameNotFoundException {

    var rawUser = getUserByKey(key);
    if (rawUser.isEmpty()) {
      throw new UsernameNotFoundException("User not found: " + key);
    }
    var user = rawUser.get();
    var authorities = getAuthorities(user);

    return new org.springframework.security.core.userdetails.User(user.getName(), user.getKey(), authorities);
  }
}
