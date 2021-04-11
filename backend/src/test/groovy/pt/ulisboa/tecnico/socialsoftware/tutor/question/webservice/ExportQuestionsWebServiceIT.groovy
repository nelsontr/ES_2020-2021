package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportQuestionsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def question
    def response

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)
        
        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: "a questiondto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1);
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        
        def options = new ArrayList<OptionDto>()

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(1)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_3_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(2)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_4_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(3)
        options.add(optionDto)

        and: "a question"
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionDto.setNumberOfCorrect(3)

        question = new Question(course, questionDto)
        question.setNumberOfAnswers(4)

        questionRepository.save(question)
        questionDetailsRepository.save(question.getQuestionDetails())
    }

    def "export questions with multiple correct options for course execution"() {
        given: "prepare request response"
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
            path: '/courses/' + courseExecution.getId() + '/questions/export',
            requestContentType: 'application/json'
        )

        then: "the response status is OK"
        assert map['response'].status == 200
        assert map['reader'] != null
    }

    def "student tries to export questions with multiple correct options"() {
        given: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
            User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(courseExecution)
        userRepository.save(student)
        courseExecution.addUser(student)
        
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "prepare request response"
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
            path: '/courses/' + courseExecution.getId() + '/questions/export',
            requestContentType: 'application/json'
        )

        then: "the response status is forbidden"
        assert map['response'].status == 403
        assert map['reader'] != null

        cleanup:
        userRepository.deleteById(student.getId())  
    }

    def cleanup() {
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }
}

