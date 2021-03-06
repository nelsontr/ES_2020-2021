package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceQuestion extends QuestionDetails {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.EAGER, orphanRemoval = true)
    private final List<Option> options = new ArrayList<>();

    public MultipleChoiceQuestion() {
        super();
    }

    public MultipleChoiceQuestion(Question question, MultipleChoiceQuestionDto questionDto) {
        super(question);
        setOptions(questionDto.getOptions());
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> optionDtos) {
        if (optionDtos.stream().filter(OptionDto::isCorrect).count() < 1) {
            throw new TutorException(NO_CORRECT_OPTION);
        }

        for (Option option: this.options) {
            option.remove();
        }
        this.options.clear();

        int index = 0;
        for (OptionDto optionDto : optionDtos) {
            if (optionDto.isCorrect() && optionDto.getRelevance() == null)
                optionDto.setRelevance(-1);

            optionDto.setSequence(index++);
            new Option(optionDto).setQuestionDetails(this);
        }
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public List<Integer> getCorrectOptionsId() {
        List<Integer> correctOptions =
                this.getOptions().stream()
                        .filter(Option::isCorrect)
                        .map(Option::getId)
                        .collect(Collectors.toList());
        if (correctOptions.isEmpty()){ return  null; }
        return correctOptions;
    }

    public List<Integer> getCorrectOptionsByRelevance() {
        List<Integer> correctOptionsOrdered =
                this.getOptions().stream()
                        .filter(Option::isCorrect)
                        .sorted(Comparator.comparingInt(Option::getRelevance))
                        .map(Option::getSequence)
                        .collect(Collectors.toList());
        if (correctOptionsOrdered.isEmpty()){ return  null; }
        return correctOptionsOrdered;
    }

    public void update(MultipleChoiceQuestionDto questionDetails) {
        setOptions(questionDetails.getOptions());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        String correctAnswersRep = "";
        List<Integer> correctAnswers = this.getCorrectAnswer();
        for (Integer correctAnswer: correctAnswers){
            correctAnswersRep += convertSequenceToLetter(correctAnswer);
            correctAnswersRep += " | ";
        }
        correctAnswersRep = correctAnswersRep.substring(0, correctAnswersRep.length() -3);
        return correctAnswersRep;

    }

    public String getCorrectOrderRepresentation(){
        String correctOrderRep = "";
        List<Integer> correctOrder = this.getCorrectOptionsByRelevance();
        for (Integer correctAnswer: correctOrder){
            Integer relevance = this.getOptions().stream()
                    .filter(x -> x.getSequence().equals(correctAnswer))
                    .findAny()
                    .map(Option::getRelevance)
                    .orElse(null);
            correctOrderRep += convertSequenceToLetter(correctAnswer);
            correctOrderRep += "("+String.valueOf(relevance)+") | ";
        }
        correctOrderRep = correctOrderRep.substring(0, correctOrderRep.length() -3);
        return correctOrderRep;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitOptions(Visitor visitor) {
        for (Option option : this.getOptions()) {
            option.accept(visitor);
        }
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new MultipleChoiceCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new MultipleChoiceStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new MultipleChoiceQuestionDto(this);
    }

    public List<Integer> getCorrectAnswer() {
        List<Integer> correctAnswers = new ArrayList<Integer>();

        this.getOptions().stream()
                .filter(Option::isCorrect)
                .forEach(x -> correctAnswers.add(x.getSequence()));

        if (correctAnswers.isEmpty()){ throw new TutorException(NO_CORRECT_OPTION); }
        return correctAnswers;

    }

    @Override
    public void delete() {
        super.delete();
        for (Option option : this.options) {
            option.remove();
        }
        this.options.clear();
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestion{" +
                "options=" + options +
                '}';
    }

    public static String convertSequenceToLetter(Integer correctAnswer) {
        return correctAnswer != null ? Character.toString('A' + correctAnswer) : "-";
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        var result = this.options
                .stream()
                .filter(x -> selectedIds.contains(x.getId()))
                .map(x -> convertSequenceToLetter(x.getSequence()))
                .collect(Collectors.joining("|"));
        return !result.isEmpty() ? result : "-";
    }
}
