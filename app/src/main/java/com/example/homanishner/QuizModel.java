package com.example.homanishner;

import com.google.firebase.firestore.DocumentSnapshot;

public class QuizModel {

    private String correctAnswer;
    private String questionWord;
    private String documentId;

    public QuizModel() {}

    public QuizModel(String correctAnswer, String questionWord) {
        this.correctAnswer = correctAnswer;
        this.questionWord = questionWord;
    }

    public String getCorrectAnswer() {
        return correctAnswer.substring(0,1).toUpperCase()+correctAnswer.substring(1);
    }

    public String getQuestionWord() {
        return questionWord.substring(0,1).toUpperCase()+questionWord.substring(1);
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(DocumentSnapshot documentSnapshot) {
        this.documentId = documentSnapshot.getId();
    }
}