package com.nikolaynikolov.primenumberapi.repository;

import com.nikolaynikolov.primenumberapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findFirstByKey(String key);
}
