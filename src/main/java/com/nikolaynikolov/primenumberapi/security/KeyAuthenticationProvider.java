package com.nikolaynikolov.primenumberapi.security;

import com.nikolaynikolov.primenumberapi.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.ws.rs.ForbiddenException;

@Component
public class KeyAuthenticationProvider implements AuthenticationProvider {

  private final UserServiceImpl userService;

  @Autowired
  public KeyAuthenticationProvider(UserServiceImpl userService) {
    this.userService = userService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    var user = userService.getUserByKey((String) authentication.getPrincipal());
    if (user.isEmpty()) {
      throw new ForbiddenException("No such user");
    }
    return new UsernamePasswordAuthenticationToken(
        authentication.getPrincipal(),
        null,
        userService.getAuthorities(user.get()));
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
