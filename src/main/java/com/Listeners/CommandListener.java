package com.Listeners;

import com.Command;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import static com.Main.isCMD;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (isCMD(content)) {
            content = content.substring(1).toUpperCase();
            for (Command c : Command.values()) {
                if (c.name().equals(content) || content.contains(c.name())) {
                    c.execute(event);
                    break;
                }
            }
        }
    }
}
