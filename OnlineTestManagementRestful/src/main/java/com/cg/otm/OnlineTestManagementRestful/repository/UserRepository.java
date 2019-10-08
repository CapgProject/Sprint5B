package com.cg.otm.OnlineTestManagementRestful.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.otm.OnlineTestManagementRestful.dto.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	public User findByUserId(Long userId);

}
