package com.example.chatv2.messageSet;

public class Message {
    private String text; // message body
    private boolean belongsToCurrentUser; // is this message sent by us?
    private String name;
    private String color;

    public Message(String text,String name, String color, boolean belongsToCurrentUser) {
        this.text = text;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.name=name;
        this.color=color;
    }

    public String getText() {
        return text;
    }


    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

}