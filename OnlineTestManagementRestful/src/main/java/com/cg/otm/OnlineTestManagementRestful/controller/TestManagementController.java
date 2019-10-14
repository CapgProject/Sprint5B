package com.cg.otm.OnlineTestManagementRestful.controller;

import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.cg.otm.OnlineTestManagementRestful.dto.OnlineTest;
import com.cg.otm.OnlineTestManagementRestful.dto.Question;
import com.cg.otm.OnlineTestManagementRestful.dto.User;
import com.cg.otm.OnlineTestManagementRestful.exception.UserException;
import com.cg.otm.OnlineTestManagementRestful.service.OnlineTestService;




@RestController
public class TestManagementController {
	@Autowired 
	OnlineTestService testservice;

	private static final Logger logger = LoggerFactory.getLogger(TestManagementController.class);
	private static int num = 0;


	/*
	 * Author: Piyush Daswani 
	 * Description: This method takes all the test details from admin and then set those details and add the test in database 
	 * Input: A test object having all details taken as input in AddTest page 
	 * Return: Return an appropriate message
	 */
	/*Mapping for the page to display after add test form is submitted*/
	@PostMapping(value = "/addtest")
	public ResponseEntity<String> addTest(@ModelAttribute("test") OnlineTest test) {
		OnlineTest testOne = new OnlineTest();
		try {
			Set<Question> question = new HashSet<Question>();
			testOne.setTestName(test.getTestName());
			testOne.setTestTotalMarks(new Double(0));
			testOne.setTestDuration(test.getTestDuration());
			testOne.setStartTime(test.getStartTime());
			testOne.setEndTime(test.getEndTime());
			testOne.setTestMarksScored(new Double(0));
			testOne.setIsdeleted(false);
			testOne.setIsTestAssigned(false);
			testOne.setTestQuestions(question);
			testservice.addTest(testOne);
		} catch (UserException e) {
			return new ResponseEntity<String>("Data not added", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>(testOne.toString(),HttpStatus.OK);
	}

	/*
	 * Author: Swanand Pande 
	 * Description: This method takes test id as input and then takes the excel file and if file is properly validated then it is transferred to Excel_Files folder and then request is passed to service layer
	 * Input: An excel file containing questions and the test id in which questions are to be added 
	 * Return: Return an appropriate message
	 */
	@PostMapping(value = "/addquestionsubmit")
	public ResponseEntity<String> addQuestion(@RequestParam("testid") long id, @RequestParam("exfile") MultipartFile file) {
		try {
			String UPLOAD_DIRECTORY = "E:\\Excel_Files";
			String fileName = file.getOriginalFilename();
			File pathFile = new File(UPLOAD_DIRECTORY);
			if (!pathFile.exists()) {  //If the given path does not exist then create the directory
				pathFile.mkdir();
			}

			long time = new Date().getTime();
			pathFile = new File(UPLOAD_DIRECTORY + "\\" + time + fileName); //appending time to filename so that files cannot have same name
			file.transferTo(pathFile);    //Transfer the file to the given path
			testservice.readFromExcel(id, fileName, time);
		} catch (UserException | IOException e) {
			return new ResponseEntity<String>("Data could not be added!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("Data Added Successfully!", HttpStatus.OK);
	}
	
	/*
	 * Author: Piyush Daswani
	 * Description: This is a mapping to display all the tests which are not deleted and not assigned
	 */
	@RequestMapping(value = "/showalltests", method = RequestMethod.GET)
	public ResponseEntity<List<OnlineTest>> showTest() {
		List<OnlineTest> testList = testservice.getTests();
		if(testList ==null) {
			return new ResponseEntity<List<OnlineTest>>(HttpStatus.NO_CONTENT);
		}
		else {
			return new ResponseEntity<List<OnlineTest>>(testList, HttpStatus.OK);
		}
	}

	/*Mapping for the table to display all users*/
	@RequestMapping(value = "/showallusers",method=RequestMethod.GET)
	public ModelAndView showUser() {
		List<User> userList = testservice.getUsers();
		return new ModelAndView("ShowUser", "userdata", userList);
	}
	

	/*
	 * Author: Piyush Daswani
	 * Description: This method searches the test with given test Id and if the test is found then delete the test
	 * Input: Test Id of the test to be deleted
	 * Return: Return an appropriate message
	 */
	@DeleteMapping(value = "removetestsubmit")
	public ResponseEntity<?> removeTest(@RequestParam("testid") long id) {
		try {
			OnlineTest deleteTest = testservice.searchTest(id);
			OnlineTest deletedTest = testservice.deleteTest(deleteTest.getTestId());
			if(deletedTest != null) {
				return new ResponseEntity<OnlineTest>(deletedTest,HttpStatus.OK);
			}
			else {
				return new ResponseEntity<String>("Something went wrong please try again later",HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (UserException e) {
			return new ResponseEntity<String>("Test id doesnt exist", HttpStatus.BAD_REQUEST);
		}
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method searches the question with given question Id and if the question is found then delete the question
	 * Input: Question Id of the question to be deleted
	 * Return: Return an appropriate message
	 */
	@DeleteMapping(value = "removequestionsubmit")
	public ResponseEntity<?> removeQuestion(@RequestParam("questionid") long id) {
		Question deletedQuestion;
		try {
			Question question = testservice.searchQuestion(id);
			deletedQuestion = testservice.deleteQuestion(question.getOnlinetest().getTestId(), question.getQuestionId());
		} catch (UserException e) {
			return new ResponseEntity<String>("Question could not be deleted!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>(deletedQuestion.toString(), HttpStatus.OK);
	}

	/*
	 * Author: Piyush Daswani 
	 * Description: This Method is used to show the first question of the test 
	 * Input: user id
	 * Return: First Question
	 */
	@GetMapping(value = "/givetest")
	public ResponseEntity<?> showQuestion(@RequestParam("userid") long userId) {
		logger.info("Entered Give test method");
		User currentUser;
		try {
			currentUser = testservice.searchUser(userId);
			if (currentUser.getUserTest() == null) {
				logger.info("No Test was assigned");
				return new ResponseEntity<String>("No Test Assigned", HttpStatus.BAD_REQUEST);
			} else {
				if (num < currentUser.getUserTest().getTestQuestions().toArray().length) {
					num++;
					logger.info("Dispayed 1st question successflly");
					return new ResponseEntity<String>(
							currentUser.getUserTest().getTestQuestions().toArray()[num - 1].toString(), HttpStatus.OK);

				} else {
					num = 0;
					logger.info("Test didn't contain any questions");
					return new ResponseEntity<String>("No Questions in the test", HttpStatus.NO_CONTENT);
				}
			}

		} catch (UserException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<String>("User id was incorrect", HttpStatus.NO_CONTENT);
		}
	}

	/*
	 * Author: Piyush Daswani 
	 * Description: This Method is used to add the chosen answer for a question and print the next question 
	 * Input: user id
	 * Return: First Question
	 */
	@PutMapping(value = "/givetest")
	public ResponseEntity<?> submitQuestion(@RequestParam("userid") long userId, @RequestParam("questId") long questId, @RequestParam("chosenanswer") int chosenAnswer) {
		logger.info("Entered Give test method");
		User currentUser;
		try {
			currentUser = testservice.searchUser(userId);
			if (currentUser.getUserTest() == null) {
				logger.info("No Test was assigned");
				return new ResponseEntity<String>("No Test Assigned", HttpStatus.BAD_REQUEST);
			} else {
				Question quest = (Question) currentUser.getUserTest().getTestQuestions().toArray()[num - 1];
				quest.setChosenAnswer(chosenAnswer);
				testservice.updateQuestion(currentUser.getUserTest().getTestId(), quest.getQuestionId(), quest);
				if (num < currentUser.getUserTest().getTestQuestions().toArray().length) {
					num++;
					logger.info("Dispayed next question successflly");
					return new ResponseEntity<String>(
							currentUser.getUserTest().getTestQuestions().toArray()[num - 1].toString(), HttpStatus.OK);

				} else {
					num = 0;
					logger.info("No more questions left");
					return new ResponseEntity<String>("Test Complete", HttpStatus.OK);
				}
			}

		} catch (UserException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<String>("User id was incorrect", HttpStatus.NO_CONTENT);
		}
	}


	/*
	 * Author: Swanand Pande
	 * Description: This Method is used to assign a test to the user
	 * Input: Test Id to be assigned and User Id of user to whom test is to assigned
	 * Return: Return an appropriate message
	 */
	@PostMapping(value = "assigntestsubmit")
	public ResponseEntity<?> assignTest(@RequestParam("testid") long testId, @RequestParam("userid") long userId) {
		try {
			testservice.assignTest(userId, testId);
			return new ResponseEntity<String>("Test assigned successfully!", HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<String>("Test could not be assigned!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getresult", method = RequestMethod.GET)
	public ModelAndView showGetResult(HttpSession session) {
		User currentUser = (User) session.getAttribute("user");
		OnlineTest test;
		try {
			test = testservice.searchTest(currentUser.getUserTest().getTestId());
			Double marksScored = test.getTestMarksScored();
			test.setTestMarksScored(new Double(0.0));
			return new ModelAndView("GetResult", "result", marksScored);

		} catch (UserException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			return new ModelAndView("GetResult", "result", 0.0);
		}

	}

	/*
	 * Author: Piyush Daswani
	 * Description: This method searches the test with given test Id and if the test is found then return the test details and show the UpdateTestDetails page included in UpdateTest page
	 * Input: Test Id of the test to be deleted
	 * Return: Return an appropriate message
	 */
	@GetMapping(value = "/updatetestinput")
	public ResponseEntity<?> updateTest(@RequestParam("testid") long id) {
		OnlineTest test;
		try {
			test = testservice.searchTest(id);
			return new ResponseEntity<OnlineTest>(test,HttpStatus.OK);
		} catch (UserException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<String>("Test not found", HttpStatus.NO_CONTENT);
		}
	}

	/*
	 * Author: Piyush Daswani
	 * Description: This method sets the updated details in a test object and passes the details to service layer
	 * Input: Test Id of the test to be updated and the object containing updated test details
	 * Return: Return an appropriate message
	 */
	@PutMapping(value = "/updatetestinput")
	public ResponseEntity<?> actualUpdate(@ModelAttribute("test") OnlineTest test) {
		OnlineTest testOne = new OnlineTest();
		Set<Question> questions = new HashSet<Question>();
		testOne.setTestId(test.getTestId());
		testOne.setTestName(test.getTestName());
		testOne.setTestDuration(test.getTestDuration());
		testOne.setStartTime(test.getStartTime());
		testOne.setEndTime(test.getEndTime());
		testOne.setIsdeleted(false);
		testOne.setTestMarksScored(new Double(0));
		testOne.setTestTotalMarks(new Double(0));
		testOne.setTestQuestions(questions);
		testOne.setIsTestAssigned(false);
		try {
			OnlineTest returnedTest = testservice.updateTest(test.getTestId(), testOne);
			return new ResponseEntity<OnlineTest>(returnedTest, HttpStatus.OK);
		} catch (UserException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<String>("The test wasnt updated due to some error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method searches the question with given question Id and if the question is found then return the question details and show the UpdateQuestionDetails page included in UpdateQuestion page
	 * Input: Question Id of the question to be updated
	 * Return: Return an appropriate message
	 */
	@GetMapping(value = "/updatequestioninput")
	public ResponseEntity<?> updateQuestion(@RequestParam("questionid") long id,
			@ModelAttribute("question") Question question) {
		Question questionOne;
		try {
			questionOne = testservice.searchQuestion(id);
			return new ResponseEntity<String>(questionOne.toString(), HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<String>("Question not found!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method sets the updated details in a question object and passes the details to service layer
	 * Input: Test Id of the test which contains the question to be updated and the object containing updated question details
	 * Return: Return an appropriate message
	 */
	@PutMapping(value = "/updatequestionsubmit")
	public ResponseEntity<?> actualUpdate(@RequestParam("testId") long testid, @ModelAttribute("question") Question question) {

		OnlineTest test;
		Question questionOne;
		try {
			test = testservice.searchTest(testid);
			questionOne = new Question();
			questionOne.setQuestionId(question.getQuestionId());
			questionOne.setQuestionTitle(question.getQuestionTitle());
			questionOne.setQuestionOptions(question.getQuestionOptions());
			questionOne.setQuestionAnswer(question.getQuestionAnswer());
			questionOne.setQuestionMarks(question.getQuestionMarks());
			questionOne.setChosenAnswer(0);
			questionOne.setIsDeleted(false);
			questionOne.setMarksScored(new Double(0));
			questionOne.setOnlinetest(test);
			testservice.updateQuestion(testid, question.getQuestionId(), questionOne);
		} catch (UserException e) {
			return new ResponseEntity<String>("Question could not be updated!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>(questionOne.toString(), HttpStatus.OK);
	}

	@RequestMapping(value = "/updateuser", method = RequestMethod.GET)
	public ModelAndView showUpdateUser(@ModelAttribute("user") User user, HttpSession session) {
		User originalUser = (User) session.getAttribute("user");
		if (originalUser.getIsAdmin()) {
			return new ModelAndView("UpdateAdminDetails", "Update", session.getAttribute("user"));
		}
		else {
			return new ModelAndView("UpdateUserDetails", "Update", session.getAttribute("user"));
		}
	}

	@RequestMapping(value = "/updateusersubmit", method = RequestMethod.POST)
	public String actualUpdate(@ModelAttribute("user") User user, HttpSession session) {
		User originalUser = (User) session.getAttribute("user");
		try {
			User userOne = testservice.searchUser(user.getUserId());
			userOne.setUserName(user.getUserName());
			userOne.setUserPassword(user.getUserPassword());
			userOne.setIsDeleted(false);

			userOne.setIsAdmin(originalUser.getIsAdmin());
			testservice.updateProfile(userOne);
		} catch (UserException e) {
			System.out.println(e.getMessage());
		}
		if (originalUser.getIsAdmin()) {
			return "admin";
		} else {
			return "user";
		}
	}

	/*
	 * Author: Swanand Pande
	 * Description: This method displays all the questions which are present in a given test
	 * Input: Test Id of the test whose questions are to be returned
	 * Return: Return an appropriate message
	 */
	@GetMapping(value = "/listquestionsubmit")
	public ResponseEntity<?> submitListQuestion(@RequestParam("testId") long testId) {
		try {
			OnlineTest test = testservice.searchTest(testId);
			List<Long> qlist = new ArrayList<Long>();
			Set<Question> questions = test.getTestQuestions();
			questions.forEach(question->{
				if(question.getIsDeleted()!=true) {
					qlist.add(question.getQuestionId());
				}
			});
			return new ResponseEntity<List<Long>>(qlist, HttpStatus.OK);
		} catch (UserException e) {
			return new ResponseEntity<String>("No Questions found!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
