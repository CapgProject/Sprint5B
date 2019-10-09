package com.cg.otm.OnlineTestManagementRestful.dao;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.cg.otm.OnlineTestManagementRestful.dto.OnlineTest;
import com.cg.otm.OnlineTestManagementRestful.dto.Question;
import com.cg.otm.OnlineTestManagementRestful.dto.User;
import com.cg.otm.OnlineTestManagementRestful.exception.ExceptionMessage;
import com.cg.otm.OnlineTestManagementRestful.exception.UserException;



@Repository("testdao")
public class OnlineTestDaoImpl implements OnlineTestDao {

	@PersistenceContext
	EntityManager entitymanager;
//	static {
//
//		Properties props = System.getProperties();
//		String userDir = props.getProperty("user.dir") + "/src/main/resources/";
//		PropertyConfigurator.configure(userDir + "log4j.properties");
//		myLogger = Logger.getLogger("OnlineTestDaoImpl.class");
//	}

	public Set<Question> getQuestionSet(Long testId) throws UserException {
		OnlineTest test = entitymanager.find(OnlineTest.class, testId);
		if (test != null && test.getTestQuestions() != null) {
			return test.getTestQuestions();
		} else {
			throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
		}
	}

	@Override
	public OnlineTest saveTest(OnlineTest onlineTest) throws UserException {
		entitymanager.persist(onlineTest);
		return onlineTest;
	}

	@Override
	public OnlineTest searchTest(Long testId) throws UserException {
		OnlineTest test = entitymanager.find(OnlineTest.class, testId);
		if (test != null && test.getIsdeleted() == false) {
			return test;
		} else {
			throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
		}
	}

	@Override
	public OnlineTest removeTest(Long testId) throws UserException {
		OnlineTest test = entitymanager.find(OnlineTest.class, testId);
		if (test != null) {
			test.setIsdeleted(true);
			Set<Question> question = test.getTestQuestions();
			question.forEach(quest -> {
				quest.setOnlinetest(null);
			});
			return test;
		} else {
			throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
		}
	}

	@Override
	public OnlineTest updateTest(OnlineTest test) throws UserException {
		OnlineTest foundTest = entitymanager.find(OnlineTest.class, test.getTestId());
		if (foundTest != null) {
			foundTest.setTestId(test.getTestId());
			foundTest.setTestName(test.getTestName());
			foundTest.setTestTotalMarks(test.getTestTotalMarks());
			foundTest.setTestQuestions(test.getTestQuestions());
			foundTest.setTestDuration(test.getTestDuration());
			foundTest.setStartTime(test.getStartTime());
			foundTest.setEndTime(test.getEndTime());
			foundTest.setIsTestAssigned(test.getIsTestAssigned());
			foundTest.setIsdeleted(test.getIsdeleted());
			foundTest.setTestMarksScored(test.getTestMarksScored());
			return foundTest;
		} else {
			throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
		}
	}

	@Override
	public Question saveQuestion(Question question) throws UserException {
		OnlineTest test = entitymanager.find(OnlineTest.class, question.getOnlinetest().getTestId());
		entitymanager.persist(question);
		test.getTestQuestions().add(question);
		return question;
	}

	@Override
	public Question searchQuestion(Long questId) throws UserException {
		Question question = entitymanager.find(Question.class, questId);
		if (question != null) {
			return question;
		} else {
			throw new UserException(ExceptionMessage.QUESTIONMESSAGE);
		}
	}

	@Override
	public Question removeQuestion(Long questId) throws UserException {
		Question question = entitymanager.find(Question.class, questId);
		if (question != null) {
			question.setIsDeleted(true);
			OnlineTest onlineTest = entitymanager.find(OnlineTest.class, question.getOnlinetest().getTestId());
			if (onlineTest != null) {
				onlineTest.getTestQuestions().remove(question);
			}
			return question;
		} else {
			throw new UserException(ExceptionMessage.QUESTIONMESSAGE);
		}
	}

	@Override
	public Question updateQuestion(Question question) throws UserException {
		Question foundQuestion = entitymanager.find(Question.class, question.getQuestionId());
		if (foundQuestion != null) {
			foundQuestion.setQuestionId(question.getQuestionId());
			foundQuestion.setQuestionTitle(question.getQuestionTitle());
			foundQuestion.setQuestionOptions(question.getQuestionOptions());
			foundQuestion.setQuestionMarks(question.getQuestionMarks());
			foundQuestion.setChosenAnswer(question.getChosenAnswer());
			foundQuestion.setIsDeleted(question.getIsDeleted());
			foundQuestion.setMarksScored(question.getMarksScored());
			foundQuestion.setQuestionAnswer(question.getQuestionAnswer());
			return foundQuestion;
		} else {
			throw new UserException(ExceptionMessage.QUESTIONMESSAGE);
		}
	}

	@Override
	public User saveUser(User user) throws UserException {
		entitymanager.persist(user);
		return user;
	}

	@Override
	public User searchUser(Long userId) throws UserException {
		User user = entitymanager.find(User.class, userId);
		if (user != null && user.getIsDeleted() == false) {
			return user;
		} else {
			throw new UserException(ExceptionMessage.USERMESSAGE);
		}
	}

	@Override
	public User removeUser(Long userId) throws UserException {
		User user = entitymanager.find(User.class, userId);
		if (user != null) {
			user.setIsDeleted(true);
			return user;
		} else {
			throw new UserException(ExceptionMessage.USERMESSAGE);
		}
	}

	@Override
	public User updateUser(User user) throws UserException {
		User foundUser = entitymanager.find(User.class, user.getUserId());
		if (foundUser.getUserTest() != null) {
			OnlineTest foundTest = entitymanager.find(OnlineTest.class, foundUser.getUserTest().getTestId());
			if (foundUser != null) {
				foundUser.setUserId(user.getUserId());
				foundUser.setUserName(user.getUserName());
				foundUser.setUserPassword(user.getUserPassword());
				foundUser.setUserTest(foundTest);
				foundUser.setIsAdmin(user.getIsAdmin());
				foundUser.setIsDeleted(user.getIsDeleted());
				return foundUser;
			} else {
				throw new UserException(ExceptionMessage.USERMESSAGE);
			}
		}
		else {
			foundUser.setUserId(user.getUserId());
			foundUser.setUserName(user.getUserName());
			foundUser.setUserPassword(user.getUserPassword());
			foundUser.setUserTest(null);
			foundUser.setIsAdmin(user.getIsAdmin());
			foundUser.setIsDeleted(user.getIsDeleted());
		}
		return foundUser;
	}

	@Override
	public List<User> getUsers() {
		Query query = entitymanager.createQuery("FROM User WHERE isAdmin IS false AND isDeleted IS false");
		List<User> userList = query.getResultList();
		return userList;
	}

	@Override
	public List<OnlineTest> getTests() {
		Query query = entitymanager.createQuery("FROM OnlineTest WHERE isDeleted IS false AND isTestAssigned IS false");
		List<OnlineTest> testList = query.getResultList();
		return testList;
	}

	@Override
	public User login(String userName, String pass) {
		Query query = entitymanager
				.createQuery("FROM User WHERE isDeleted IS false AND userName=:first AND userPassword=:second");
		query.setParameter("first", userName);
		query.setParameter("second", pass);
		try {
			User user = (User) query.getSingleResult();
			return user;
		} catch (Exception e) {
			return null;
		}

	}

}
