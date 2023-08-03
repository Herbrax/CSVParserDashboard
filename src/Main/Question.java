package Main;

import java.util.Objects;

public class Question {
    private String questionType;
    private String workerEmail;
    private String questionId;
    private String originalQuestionId;

    // Constructors, getters, setters, hashCode, equals

    public Question(String questionType, String workerEmail, String questionId, String originalQuestionId) {
        this.questionType = questionType;
        this.workerEmail = workerEmail;
        this.questionId = questionId;
        this.originalQuestionId = originalQuestionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getWorkerEmail() {
        return workerEmail;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getOriginalQuestionId() {
        return originalQuestionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return Objects.equals(getQuestionType(), question.getQuestionType()) &&
                Objects.equals(getWorkerEmail(), question.getWorkerEmail()) &&
                Objects.equals(getQuestionId(), question.getQuestionId()) &&
                Objects.equals(getOriginalQuestionId(), question.getOriginalQuestionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestionType(), getWorkerEmail(), getQuestionId(), getOriginalQuestionId());
    }

    // toCSVString() for writing to CSV
    public String toCSVString() {
        return questionType + ";" + workerEmail + ";" + questionId + ";" + originalQuestionId;
    }
}
