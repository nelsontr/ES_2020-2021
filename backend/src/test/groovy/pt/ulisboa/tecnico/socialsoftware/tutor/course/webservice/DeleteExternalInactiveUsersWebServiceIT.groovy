package pt.ulisboa.tecnico.socialsoftware.tutor.course.webservice

import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeleteExternalInactiveUsersWebServiceIT extends SpockTest{
    @LocalServerPort
    private int port

    def response
    def user1
    def user2
    def authUser1
    def authUser2

    def course1
    def courseExecution1
    def usersIdsList

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)
        usersIdsList = new ArrayList<>()
        course1 = new Course("Demo Course", Course.Type.EXTERNAL)
        courseRepository.save(course1)
        courseExecution1 = new CourseExecution(course1, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL)
        courseExecutionRepository.save(courseExecution1)
        demoAdminLogin()
    }

    def "there are two inactive external user and deletes them"() {
        given: "two inactive external users"
        user1 = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, false, pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.AuthUser.Type.EXTERNAL)
        user1.addCourse(courseExecution1)
        courseExecution1.addUser(user1)
        userRepository.save(user1)
        authUser1 = new AuthUser(user1)
        user1.setAuthUser(authUser1)
        authUserRepository.save(authUser1)

        user2 = new User(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, User.Role.TEACHER, false, false, pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.AuthUser.Type.EXTERNAL)
        user2.addCourse(courseExecution1)
        courseExecution1.addUser(user2)
        userRepository.save(user2)
        authUser2 = new AuthUser(user2)
        user2.setAuthUser(authUser2)
        authUserRepository.save(authUser2)
        and:"a user ids list"
        usersIdsList.add(user1.getId())
        usersIdsList.add(user2.getId())

        when:
        response = restClient.post(
                path: '/executions/'+courseExecution1.getId()+'/users/delete/',
                body:
                        usersIdsList,
                requestContentType: 'application/json'
        )

        then: "check response status"
        response.status == 200
        and: "the users were removed from the database"
        userRepository.findById(user1.getId()).isEmpty()
        userRepository.findById(user2.getId()).isEmpty()

        cleanup:
        courseExecution1.remove()
        courseExecutionRepository.dissociateCourseExecutionUsers(courseExecution1.getId())
        courseExecutionRepository.deleteById(courseExecution1.getId())
        courseRepository.deleteById(course1.getId())
    }

    def cleanup() {
        persistentCourseCleanup()
    }
}