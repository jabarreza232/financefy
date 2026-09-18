package id.co.evolution.financefy.model;

public class ModelFaq {

    private String question;
    private String answer;

    public ModelFaq(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
}
