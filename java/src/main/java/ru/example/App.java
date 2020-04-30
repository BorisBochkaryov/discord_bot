package ru.example;

import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class App {

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault("NzA1NDM1MTE5OTM0MTExODA2.XqrpsQ.98IyYTUiZTudX-GhpGb1DNd9bBw");
        builder.addEventListeners(new Event());
        builder.build();
    }
}
