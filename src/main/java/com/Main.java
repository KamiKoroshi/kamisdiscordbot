package com;

import com.Listeners.CommandListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;

import javax.security.auth.login.LoginException;

public class Main {

    private static JDA jda;
    private static String prefix = "!";

    public static void main(String[] args) {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken("NDg2MjEyMjA5ODk1Mjc2NTQ0.Dm8EEA.x5OayB06e7XLA8AU7ybn6lT79Gk")
                    .setStatus(OnlineStatus.ONLINE)
                    .setAutoReconnect(false)
                    .buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jda.addEventListener(new CommandListener());
    }

    public static boolean isCMD(String input) {
        return input.startsWith("!");
    }

}
