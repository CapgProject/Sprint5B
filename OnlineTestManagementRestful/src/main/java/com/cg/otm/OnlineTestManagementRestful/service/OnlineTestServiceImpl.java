package com.cg.otm.OnlineTestManagementRestful.service;
/**
 * Author: Swanand Pande,Piyush Daswani, Priya Kumari
 * Description: Serice class for handling all the Business Layer logic
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;

import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cg.otm.OnlineTestManagementRestful.dto.OnlineTest;
import com.cg.otm.OnlineTestManagementRestful.dto.Question;
import com.cg.otm.OnlineTestManagementRestful.dto.User;
import com.cg.otm.OnlineTestManagementRestful.exception.ExceptionMessage;
import com.cg.otm.OnlineTestManagementRestful.exception.UserException;
import com.cg.otm.OnlineTestManagementRestful.repository.OnlineTestRepository;
import com.cg.otm.OnlineTestManagementRestful.repository.QuestionRepository;
import com.cg.otm.OnlineTestManagementRestful.repository.UserRepository;
@Service("testservice")
@Transactional
public class OnlineTestServiceImpl implements OnlineTestService{
	@Autowired
	QuestionRepository questionRepository;
	@Autowired
	OnlineTestRepository testRepository;
	
	@Autowired
	UserRepository userRepository;
    
	/*
	 * Author: Piyush Daswani 
	 * Description: This Method is used to add the user to the database 
	 * Input: user object
	 * Return: added object
	 */
	@Override
	public User registerUser(User user) throws UserException {
		User returnedUser = userRepository.save(user);
		if(returnedUser != null)
			return user;
		else {
			throw new UserException(ExceptionMessage.DATABASEMESSAGE);
		}
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method finds the question with given id and if found return it else if not found or if test does not contain that question then display appropriate message 
	 * Input: Question Id and the Test object which contains the question
	 * Return: Return the desired question object
	 */
	@Override
	public Question showQuestion(OnlineTest onlineTest, Long questionId) throws UserException {
		Question question = questionRepository.findByQuestionId(questionId);
		if (question == null || !onlineTest.getTestQuestions().contains(question)) {
			throw new UserException(ExceptionMessage.QUESTIONMESSAGE);
		}
		return question;
	}

	@Override
	public Boolean assignTest(Long userId, Long testId) throws UserException {
		User user = userRepository.findByUserId(userId);
		OnlineTest onlineTest = testRepository.findByTestId(testId);
		if (user == null) {
			throw new UserException(ExceptionMessage.USERMESSAGE);
		}
		if (user.getIsAdmin()) {
			throw new UserException(ExceptionMessage.ADMINMESSAGE);
		}
		if (onlineTest == null) {
			throw new UserException(ExceptionMessage.TESTMESSAGE);
		}
		if (onlineTest.getIsTestAssigned()) {
			throw new UserException(ExceptionMessage.TESTASSIGNEDMESSAGE);
		} else {
			user.setUserTest(onlineTest);
			onlineTest.setIsTestAssigned(true);
		}
		testRepository.save(onlineTest);
		userRepository.save(user);
		return true;
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method adds the object in database and if it could not add then message is displayed
	 * Input: Test object to be added
	 * Return: Return the test object added
	 */
	@Override
	public OnlineTest addTest(OnlineTest onlineTest) throws UserException {
		OnlineTest returnedTest = testRepository.save(onlineTest);
		if (returnedTest == null) {
			throw new UserException(ExceptionMessage.DATABASEMESSAGE);
		}
		return returnedTest;
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method finds the test by the given id and sets the updated details in a object and save it in database
	 * Input: Test Id of the test to be updated and the test object containing updated test details
	 * Return: Return the updated test object
	 */
	@Override
	public OnlineTest updateTest(Long testId, OnlineTest onlineTest) throws UserException {
		OnlineTest temp = testRepository.findByTestId(testId);
		if (temp != null) {
			temp.setTestId(testId);
			temp.setTestName(onlineTest.getTestName());
			temp.setTestDuration(onlineTest.getTestDuration());
			temp.setStartTime(onlineTest.getStartTime());
			temp.setEndTime(onlineTest.getEndTime());
			temp.setIsdeleted(onlineTest.getIsdeleted());
			temp.setIsTestAssigned(onlineTest.getIsTestAssigned());
			temp.setTestMarksScored(onlineTest.getTestMarksScored());
			temp.setTestQuestions(onlineTest.getTestQuestions());
			temp.setTestTotalMarks(onlineTest.getTestMarksScored());
			testRepository.save(temp);
			return onlineTest;
		} else
			throw new UserException(ExceptionMessage.TESTMESSAGE);
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method finds the test and if found sets the isDeleted flag as true
	 * Input: Test Id of the test to be deleted
	 * Return: Return the object of deleted test
	 */
	@Override
	public OnlineTest deleteTest(Long testId) throws UserException {
		OnlineTest returnedTest = testRepository.findByTestId(testId);
		if (returnedTest != null && returnedTest.getIsdeleted() == false) {
			returnedTest.setIsdeleted(true);
		} else {
			throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
		}
		return returnedTest;
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method finds the test and then checks if it contains the question, if found then update the question details else display appropriate message
	 * Input: Test Id of the test containing question, question id of question to be updated and the updated question details in a object
	 * Return: Return the updated question object
	 */
	@Override
	public Question updateQuestion(Long testId, Long questionId, Question question) throws UserException {
		OnlineTest temp = testRepository.findByTestId(testId);
		if (temp != null) {
			Set<Question> quests = temp.getTestQuestions();
			Question tempQuestion = questionRepository.findByQuestionId(questionId);
			if (tempQuestion != null && quests.contains(tempQuestion)) {
				tempQuestion.setChosenAnswer(question.getChosenAnswer());
				if (tempQuestion.getChosenAnswer() == tempQuestion.getQuestionAnswer()) {
					tempQuestion.setMarksScored(question.getQuestionMarks());
				}
				question.setQuestionId(questionId);
				temp.setTestTotalMarks(
						temp.getTestTotalMarks() - tempQuestion.getQuestionMarks() + question.getQuestionMarks());
				temp.setTestMarksScored(temp.getTestMarksScored() + tempQuestion.getMarksScored());
				tempQuestion.setQuestionMarks(question.getQuestionMarks());
				tempQuestion.setIsDeleted(question.getIsDeleted());
				tempQuestion.setOnlinetest(question.getOnlinetest());
				tempQuestion.setQuestionAnswer(question.getQuestionAnswer());
				tempQuestion.setQuestionId(questionId);
				tempQuestion.setQuestionOptions(question.getQuestionOptions());
				tempQuestion.setQuestionTitle(question.getQuestionTitle());
				questionRepository.save(tempQuestion);
				testRepository.save(temp);
				return question;
			} else
				throw new UserException(ExceptionMessage.QUESTIONMESSAGE);
		} else
			throw new UserException(ExceptionMessage.TESTMESSAGE);
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method finds the test and if it contains the question then set question isDeleted flag as true and subtract question marks from the test total marks
	 * Input: Test Id of the test containing question and question id of question to be deleted
	 * Return: Return the deleted question object
	 */
	@Override
	public Question deleteQuestion(Long testId, Long questionId) throws UserException {
		OnlineTest temp = testRepository.findByTestId(testId);
		if (temp != null) {
			Set<Question> quests = temp.getTestQuestions();
			Question tempQuestion = questionRepository.findByQuestionId(questionId);
			if (tempQuestion != null && quests.contains(tempQuestion) && tempQuestion.getIsDeleted() == false) {
				temp.setTestTotalMarks(temp.getTestTotalMarks() - tempQuestion.getQuestionMarks());
				testRepository.save(temp);
				tempQuestion.setIsDeleted(true);
				return tempQuestion;
			} else
				throw new UserException(ExceptionMessage.QUESTIONMESSAGE);
		} else
			throw new UserException(ExceptionMessage.TESTMESSAGE);
	}
	
	/*
	 * Author: Piyush Daswani 
	 * Description: This Method is used to get result from user
	 * Input: test object
	 * Return: result object
	 */
	@Override
	public Double getResult(OnlineTest onlineTest) throws UserException {
		Double score = calculateTotalMarks(onlineTest);
		onlineTest.setIsTestAssigned(false);
		testRepository.save(onlineTest);
		return score;
	}

	/*
	 * Author: Piyush Daswani 
	 * Description: This Method is used to calculate the total marks 
	 * Input: test object
	 * Return: score
	 */
	@Override
	public Double calculateTotalMarks(OnlineTest onlineTest) throws UserException {
		Double score = new Double(0.0);
		for (Question question : onlineTest.getTestQuestions()) {
			score = score + question.getMarksScored();
		}
		onlineTest.setTestMarksScored(score);
		testRepository.save(onlineTest);
		return score;
	}
	
	/*
	 * Author: Priya 
	 * Description: This Method is used to search the user from the database 
	 * Input: user id
	 * Return: found object
	 */
	@Override
	public User searchUser(Long userId) throws UserException {
		User returnedUser = userRepository.findByUserId(userId);
		if (returnedUser != null) {
			return returnedUser;
		} else {
			throw new UserException(ExceptionMessage.USERMESSAGE);
		}

	}

	/*
	 * Author: Swanand Pande
	 * Description: This method finds the test with given id and if not found then display appropriate message
	 * Input: Test Id of the test to be searched
	 * Return: Return the found test object
	 */
	@Override
	public OnlineTest searchTest(Long testId) throws UserException {
		OnlineTest returnedTest = testRepository.findByTestId(testId);
		if (returnedTest != null) {
			return returnedTest;
		} else {
			throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
		}
	}
	
	/*
	 * Author: Priya
	 * Description: This Method is used to update the user to the database 
	 * Input: user object
	 * Return: updated object
	 */     
	@Override
	public User updateProfile(User user) throws UserException {

		User returnedUser = userRepository.findById(user.getUserId()).orElse(null);
		if (returnedUser == null) {
			throw new UserException(ExceptionMessage.USERMESSAGE);
		}
		try {
			userRepository.save(user);
			return user;
		}
		catch(Exception e){
			throw new UserException("Username already exists!");
		}
	}
	 
	/*
	 * Author: Piyush Daswani 
	 * Description: This Method is used to list the user from the database 
	 * Input:
	 * Return: List of user objects
	 */
	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method returns all tests which are not deleted and not assigned
	 * Return: Return list of tests
	 */
	@Override
	public List<OnlineTest> getTests()  {
		// TODO Auto-generated method stub
		ArrayList<OnlineTest> testList = new ArrayList<OnlineTest>( );
		
		 List<OnlineTest> test= testRepository.findAllNotAssignedAndNotDeleted();
		 for (OnlineTest onlineTest : test) {

			 if(onlineTest.getTestQuestions()!=null) {
				 OnlineTest newtest=new OnlineTest();
				   newtest.setTestId(onlineTest.getTestId());
				   newtest.setTestName(onlineTest.getTestName());
				   newtest.setTestDuration(onlineTest.getTestDuration());
				   newtest.setStartTime(onlineTest.getStartTime());
				   newtest.setEndTime(onlineTest.getEndTime());
				   testList.add(newtest);
				 }
			}
		 return testList;
		//return testRepository.findAllNotAssignedAndNotDeleted();
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method finds the question with given id and if not found then displays appropriate message
	 * Input: Question Id of the question to be found
	 * Return: Return the question object found
	 */
	@Override
	public Question searchQuestion(Long questionId) throws UserException {
		Question question = questionRepository.findByQuestionId(questionId);
		if (question != null) {
			return question;
		} else {
			throw new UserException(ExceptionMessage.QUESTIONNOTFOUNDMESSAGE);
		}
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method reads the question details from an excel file and adds the questions to the database
	 * Input: Test id in which questions are to be added, filename of excel and time to be appended to filename
	 */
	@Override
	public void readFromExcel(long id, String fileName, long time) throws IOException, UserException {
		String UPLOAD_DIRECTORY = "E:\\Excel_Files";
		File dataFile = new File(UPLOAD_DIRECTORY + "\\" + time + fileName);
		FileInputStream fis = new FileInputStream(dataFile);
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
		XSSFSheet sheet = workbook.getSheetAt(0);    //Read the first sheet
		Row row;
		OnlineTest test = testRepository.findByTestId(id);   //Find the test
		if(test!= null) {
		Double testMarks = test.getTestTotalMarks();
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			row = (Row) sheet.getRow(i);
			String title;
			if (row.getCell(0) == null) {
				throw new UserException(ExceptionMessage.QUESTIONTITLEMESSAGE);   //If Title is not present
			} else { 
				title = row.getCell(0).toString();
			}
			String marks;
			if (row.getCell(1) == null) {
				throw new UserException(ExceptionMessage.QUESTIONMARKSMESSAGE);  //If Marks is not present
			} else {
				marks = row.getCell(1).toString();
				testMarks = testMarks + Double.parseDouble(marks);
			}
			String options;
			if (row.getCell(2) == null) {
				throw new UserException(ExceptionMessage.QUESTIONOPTIONSMESSAGE);   //If Options is not present
			} else {
				options = row.getCell(2).toString();
			}
			String answer;
			if (row.getCell(3) == null) {
				throw new UserException(ExceptionMessage.QUESTIONANSWERMESSAGE);   //If Answer is not present
			} else {
				answer = row.getCell(3).toString();
			}

			Question question = new Question();
			
			if(test == null) {
				throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
			}
			test.setTestTotalMarks(testMarks);
			String option[] = new String[4];
			question.setQuestionTitle(title);
			question.setQuestionMarks(Double.parseDouble(marks));
			StringTokenizer token = new StringTokenizer(options, ",");
			int k = 0;
			while (token.hasMoreTokens()) {    //separate the options by splitting with comma
				option[k] = token.nextToken();
				k++;
			}
			question.setQuestionOptions(option);
			question.setQuestionAnswer((int) Double.parseDouble(answer));
			question.setChosenAnswer(0);
			question.setIsDeleted(false);
			question.setMarksScored(new Double(0));
			question.setOnlinetest(test);
			questionRepository.save(question);
		}
		}
		else {
			throw new UserException(ExceptionMessage.NOTESTMESSAGE);
		}
		fis.close();
	}
	
	@Override
	public Question addQuestion(long id, Question question) throws UserException {
		
		OnlineTest test = testRepository.findByTestId(id);
		if(test != null) {
			questionRepository.save(question);
			Set<Question> questions = test.getTestQuestions();
			questions.add(question);
			test.setTestQuestions(questions);
			test.setTestTotalMarks(test.getTestTotalMarks() + question.getQuestionMarks());
			testRepository.save(test);
		}
		else {
			throw new UserException(ExceptionMessage.TESTNOTFOUNDMESSAGE);
		}
		return question;
	}

	@Override
	public User searchUserByName(String name) throws UserException {
		Optional<User> returnedUser = userRepository.findByUserName(name);
		System.out.println(returnedUser.get());
		if(returnedUser.get() != null)
			return returnedUser.get();
		else {
			throw new UserException(ExceptionMessage.USERMESSAGE);
		}
	}

}
