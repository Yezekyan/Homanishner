package com.example.homanishner;

public class User {
    private String display_name;
    private int score;
    private int userId;

    public User() {

    }

    public User(String username, int score) {
        this.display_name = username;
        this.score = score;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public int getScore() {
        return score;
    }
}