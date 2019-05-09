package com.aelkz.springboot.skeleton.repository;

import com.aelkz.springboot.skeleton.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
