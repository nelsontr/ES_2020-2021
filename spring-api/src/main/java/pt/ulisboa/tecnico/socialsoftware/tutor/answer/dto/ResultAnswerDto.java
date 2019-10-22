package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import java.io.Serializable;


public class ResultAnswerDto implements Serializable {
    private Integer quizQuestionId;
    private Integer optionId;
    private Integer timeTaken;
    private Integer sequence;

    public ResultAnswerDto(){
    }

    public ResultAnswerDto(Integer quizQuestionId, Integer optionId, Integer timeTaken){
        this.quizQuestionId = quizQuestionId;
        this.optionId = optionId;
        this.timeTaken = timeTaken;
    }

    public Integer getQuizQuestionId() {
        return quizQuestionId;
    }

    public void setQuizQuestionId(Integer quizQuestionId) {
        this.quizQuestionId = quizQuestionId;
    }

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "ResultAnswerDto{" +
                "quizQuestionId=" + quizQuestionId +
                ", optionId=" + optionId +
                ", timeTaken=" + timeTaken +
                ", sequence=" + sequence +
                '}';
    }
}