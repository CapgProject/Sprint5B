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

	private static int num = 0;

	/*Mapping for the home page*/
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String displayHomePage(@ModelAttribute("user") User user) {
		return "home";
	}

//	/*Mapping for the page to display add test form*/
//	@GetMapping(value = "/addtest")
//	public String showAddTest() {
//		return "AddTest";
//	}

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

//	/*Mapping for the page to display add question form*/
//	@RequestMapping(value = "/addquestion", method = RequestMethod.GET)
//	public String showAddQuestion(@ModelAttribute("question") Question question) {
//		return "AddQuestion";
//	}

	/*Mapping for the page to display after add question form is submitted*/
	@PostMapping(value = "/addquestionsubmit")
	public ResponseEntity<String> addQuestion(@RequestParam("testid") long id, @RequestParam("exfile") MultipartFile file) {
		try {
			String UPLOAD_DIRECTORY = "E:\\Excel_Files";
			String fileName = file.getOriginalFilename();
			File pathFile = new File(UPLOAD_DIRECTORY);
			if (!pathFile.exists()) {
				pathFile.mkdir();
			}

			long time = new Date().getTime();
			pathFile = new File(UPLOAD_DIRECTORY + "\\" + time + fileName);
			file.transferTo(pathFile);
			testservice.readFromExcel(id, fileName, time);
		} catch (UserException | IOException e) {
			return new ResponseEntity<String>("Data could not be added!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("Data Added Successfully!", HttpStatus.OK);
	}

	/*Mapping for the page to display add user form*/
	@RequestMapping(value = "/adduser", method = RequestMethod.GET)
	public String showAddUser(@ModelAttribute("user") User user) {
		return "AddUser";
	}

	/*Mapping for the page to display after add user form is submitted*/
	@RequestMapping(value = "/addusersubmit",method=RequestMethod.POST)
	public String addUser(@ModelAttribute("user") User user) {
		try {
			user.setUserTest(null);
			user.setIsAdmin(false);
			user.setIsDeleted(false);
			user.setUserTest(null);
			testservice.registerUser(user);
		} catch (UserException e) {
			System.out.println(e.getMessage());
		}
		return "home";
	}
	
	/*Mapping for the table to display all tests*/
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
	
	


	/*Mapping for the form to take input of test to be deleted*/
	@RequestMapping(value = "/removetest", method = RequestMethod.GET)
	public String showRemoveTest() {
		return "RemoveTest";
	}

	/*Mapping for the page after form is submitted*/
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

//	/*Mapping for the form to take input of question to be deleted*/
//	@RequestMapping(value = "/removequestion", method = RequestMethod.GET)
//	public String showRemoveQuestion() {
//		return "RemoveQuestion";
//	}

	/*Mapping for the page after form is submitted*/
	@PostMapping(value = "removequestionsubmit")
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

	/*Mapping for the page where the user can give test and see the first question*/
	@RequestMapping(value = "/givetest", method = RequestMethod.GET)
	public ModelAndView showQuestion(HttpSession session, @ModelAttribute("Question") Question question) {
		User currentUser = (User) session.getAttribute("user");
		ModelAndView mav = new ModelAndView("GiveTest");

		if (currentUser.getUserTest() == null) {
			mav.addObject("heading", "No Test Assigned Yet");
			return mav;
		} else {
			mav.addObject("heading", currentUser.getUserTest().getTestName());
			if (currentUser.getUserTest().getTestQuestions().toArray().length < num) {
				return new ModelAndView("user");
			}
			mav.addObject("questions", currentUser.getUserTest().getTestQuestions().toArray()[num]);
			num++;
			return mav;
		}
	}

	/*Mapping to display questions one at a time*/
	@RequestMapping(value = "/givetest", method = RequestMethod.POST)
	public ModelAndView submitQuestion(HttpSession session, @ModelAttribute("Question") Question question) {
		User currentUser = (User) session.getAttribute("user");
		ModelAndView mav = new ModelAndView("GiveTest");
		Question quest = (Question) currentUser.getUserTest().getTestQuestions().toArray()[num - 1];
		quest.setChosenAnswer(question.getChosenAnswer());
		System.out.println(quest);
		try {
			System.out.println(
					testservice.updateQuestion(quest.getOnlinetest().getTestId(), quest.getQuestionId(), quest));

			mav.addObject("heading", currentUser.getUserTest().getTestName());
			if (num >= currentUser.getUserTest().getTestQuestions().toArray().length) {
				num = 0;
				return new ModelAndView("user");
			} else {
				mav.addObject("questions", currentUser.getUserTest().getTestQuestions().toArray()[num]);
				num++;
				return mav;
			}
		} catch (UserException e) {
			e.printStackTrace();
			return new ModelAndView("user");
		}
	}

//	@RequestMapping(value = "assigntest", method = RequestMethod.GET)
//	public String showAssignTest() {
//		return "AssignTest";
//	}

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



//	@RequestMapping(value = "/updatetest", method = RequestMethod.GET)
//	public String showUpdateTest() {
//		return "UpdateTest";
//	}

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


	@RequestMapping(value = "/updatequestion", method = RequestMethod.GET)
	public String showUpdateQuestion(@ModelAttribute("question") Question question) {
		return "UpdateQuestion";
	}

	@RequestMapping(value = "/updatequestioninput", method = RequestMethod.POST)
	public ModelAndView updateQuestion(@RequestParam("questionid") long id,
			@ModelAttribute("question") Question question) {
		Question questionOne;
		try {
			questionOne = testservice.searchQuestion(id);
			return new ModelAndView("UpdateQuestion", "Update", questionOne);
		} catch (UserException e) {
			System.out.println(e.getMessage());
		}
		return new ModelAndView("admin");
	}

	@RequestMapping(value = "/updatequestionsubmit", method = RequestMethod.POST)
	public String actualUpdate(@RequestParam("testId") long testid, @ModelAttribute("question") Question question) {

		OnlineTest test;
		try {
			test = testservice.searchTest(testid);
			Question questionOne = new Question();
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
		} catch (UserException e1) {
			System.out.println(e1.getMessage());
		}
		return "admin";
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

	@RequestMapping(value = "/onlogin", method = RequestMethod.POST)
	public String onLogin(@ModelAttribute("user") User user, HttpSession session) {
		User foundUser = testservice.login(user.getUserName(), user.getUserPassword());
		if (foundUser != null) {
			session.setAttribute("user", foundUser);
			if (foundUser.getIsAdmin()) {
				return "admin";
			} else {
				return "user";
			}
		} else {
			return "home";
		}

	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String onLogout(HttpSession session, @ModelAttribute("user") User user) {
		session.invalidate();
		return "home";
	}

	@RequestMapping(value = "user", method = RequestMethod.GET)
	public String user() {
		return "user";
	}

	@RequestMapping(value = "admin", method = RequestMethod.GET)
	public String admin() {
		return "admin";
	}

//	@RequestMapping(value = "/listquestion", method = RequestMethod.GET)
//	public String showListQuestion() {
//		return "ListQuestion";
//	}

	@RequestMapping(value = "/listquestionsubmit", method = RequestMethod.POST)
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
