package com.nikolaynikolov.primenumberapi.configuration;

import com.nikolaynikolov.primenumberapi.security.KeyAuthenticationProvider;
import com.nikolaynikolov.primenumberapi.security.KeyRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static com.nikolaynikolov.primenumberapi.model.Permission.ACCESS_PRIME_API;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private KeyAuthenticationProvider authenticationProvider;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(authenticationProvider);
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {

    httpSecurity.csrf().disable()
        .authorizeRequests()
        .antMatchers("/prime").hasAuthority(ACCESS_PRIME_API)
        .antMatchers("/actuator/*").permitAll()
        .and()
        .addFilterBefore(new KeyRequestFilter(), BasicAuthenticationFilter.class)
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .httpBasic().disable();
  }
}
