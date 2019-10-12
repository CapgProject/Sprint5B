/**
 * 
 */
package com.cg.otm.OnlineTestManagementRestful.model;

/**
 * @author Swanand Pande
 *
 */
public class UserDTO {

	private String userName;
	private String userPassword;

	public String getUsername() {
		return userName;
	}

	public void setUsername(String username) {
		this.userName = username;
	}

	public String getPassword() {
		return userPassword;
	}

	public void setPassword(String password) {
		this.userPassword = password;
	}
}
