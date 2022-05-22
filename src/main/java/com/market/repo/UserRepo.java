package com.market.repo;

import java.util.Optional;

import com.market.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
}
