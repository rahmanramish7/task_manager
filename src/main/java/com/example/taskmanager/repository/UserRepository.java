package com.example.taskmanager.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.taskmanager.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
// Compare this snippet from taskmanager/src/main/java/com/example/taskmanager/repository/TaskRepository.java:
	