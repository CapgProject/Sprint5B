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
	private Boolean isAdmin;
	private Boolean isDeleted;
	
	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

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
