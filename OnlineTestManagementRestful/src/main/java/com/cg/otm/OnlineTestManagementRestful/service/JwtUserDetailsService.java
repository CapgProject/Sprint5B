/**
 * 
 */
package com.cg.otm.OnlineTestManagementRestful.service;
import com.cg.otm.OnlineTestManagementRestful.dto.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cg.otm.OnlineTestManagementRestful.model.UserDTO;
import com.cg.otm.OnlineTestManagementRestful.repository.UserRepository;

/**
 * @author Swanand Pande
 *
 */
@Service
public class JwtUserDetailsService implements UserDetailsService{

	@Autowired
	private UserRepository repository;

	@Autowired
	private PasswordEncoder bcryptEncoder;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = repository.findByUserName(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getUserPassword(),
				new ArrayList<>());
	}
	
	public User save(UserDTO user) {
		User newUser = new User();
		newUser.setUserName(user.getUsername());
		newUser.setUserPassword(bcryptEncoder.encode(user.getPassword()));
		return repository.save(newUser);
	}
}
