package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportMultipleChoiceQuestionsTest extends SpockTest {
    def questionId
    def questionDto
    def options
    def teacher

    def setup() {

        createExternalCourseAndExecution()

        questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)
        
        def optionDto = new OptionDto()
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(2)
        options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.setNumberOfCorrect(1)
    }

    def 'export and import questions to xml'() {
        given: 'a xml with questions'
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()

        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        and: 'a clean database'
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 1
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getOptions().size() == 2
        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        optionOneResult.getSequence() + optionTwoResult.getSequence() == 1
        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_1_CONTENT
        !(optionOneResult.isCorrect() && optionTwoResult.isCorrect())
        optionOneResult.isCorrect() || optionTwoResult.isCorrect()
    }

    def 'export questions with more than one correct option to xml'() {
        given: 'two additional correct options'
        def optionDto = new OptionDto()
        optionDto.setSequence(3)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(1)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setSequence(3)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(1)
        options.add(optionDto)
        
        questionDto.setNumberOfCorrect(3)
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()


        when:
        def questionsXml = questionService.exportQuestionsToXml()

        then:
        questionsXml != null
        print questionsXml

    }

    def 'export and import questions with more than one correct option to xml'() {
        given: 'two additional correct options'
        def optionDto = new OptionDto()
        optionDto.setSequence(2)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(3)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setSequence(3)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(1)
        options.add(optionDto)

        questionDto.setNumberOfCorrect(3)
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()

        and : 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()
        and: 'a clean database'
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 1
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        questionResult.getNumberOfCorrect() == 3
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getOptions().size() == 4
        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        def optionThreeResult = questionResult.getQuestionDetailsDto().getOptions().get(2)
        def optionFourResult = questionResult.getQuestionDetailsDto().getOptions().get(3)

        optionOneResult.getSequence() + optionTwoResult.getSequence() == 1
        optionThreeResult.getSequence() + optionFourResult.getSequence() == 5

        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_1_CONTENT
        optionThreeResult.getContent() == OPTION_2_CONTENT
        optionFourResult.getContent() == OPTION_2_CONTENT

        optionOneResult.isCorrect()
        !optionTwoResult.isCorrect()
        optionThreeResult.isCorrect()
        optionFourResult.isCorrect()

        optionOneResult.getRelevance() == 2
        optionTwoResult.getRelevance() == -1
        optionThreeResult.getRelevance() == 3
        optionFourResult.getRelevance()== 1
    }
    
    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }

    def 'export to latex with more than one correct option'() {
        given: 'two additional correct options'
        def optionDto = new OptionDto()
        optionDto.setSequence(2)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(3)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setSequence(3)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setRelevance(1)
        options.add(optionDto)

        questionDto.setNumberOfCorrect(3)
        questionDto.getQuestionDetailsDto().setOptions(options)
        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()

        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
        print questionsLatex
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
