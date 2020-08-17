package com.nikolaynikolov.primenumberapi.service;

import com.nikolaynikolov.primenumberapi.cache.Cache;
import com.nikolaynikolov.primenumberapi.cache.LocalCache;
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

  private final Cache<String, User> userCache;

  @Autowired
  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
    this.userCache = new LocalCache<>();
  }

  public Optional<User> getUserByKey(String key) {
    if (userCache.contains(key)) {
      return Optional.of(userCache.get(key));
    }
    var rawUser = this.userRepository.findFirstByKey(key);
    rawUser.ifPresent(user -> userCache.set(key, user));
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
