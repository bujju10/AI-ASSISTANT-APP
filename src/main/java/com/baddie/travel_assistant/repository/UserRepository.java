package com.baddie.travel_assistant.repository;

import com.baddie.travel_assistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
