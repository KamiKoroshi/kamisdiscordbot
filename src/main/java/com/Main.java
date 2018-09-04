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
        // Temp adding ip + password here
        props.updateRcon();

        try {
            // Using a java class called props.java for this temporarily
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(props.getToken())
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
        return input.startsWith(prefix);
    }

}
