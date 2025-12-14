package com.example.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class ChatCommand {
    private final ChatClient chatClient;

    public ChatCommand(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    @ShellMethod("chat")
    public void chat(String prompt) {
        var answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        System.out.println(answer);
    }
}
