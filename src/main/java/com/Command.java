package com;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.managers.GuildController;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;
import java.util.List;

public enum Command {

    COMMANDS("Shows all the commands"){
        @Override
        public void execute(MessageReceivedEvent event) {
            channel = event.getChannel();
            StringBuilder message = new StringBuilder();
            message.append("```\n");
            for (Command cmd : Command.values()) {
                message.append(cmd.name().toLowerCase()).append(cmd.usage).append("\n");
            }
            message.append("```");
            channel.sendMessage(message.toString()).queue();
        }
    },
    PING("Instantly sends back another messages"){
        @Override
        public void execute(MessageReceivedEvent event) {
            channel = event.getChannel();
            channel.sendMessage("Pong!").queue();
        }
    },
    ADDROLE("[ROLE] [@Name] (To give someone else the role) OR [ROLE] (To give yourself a role)"){
        @Override
        public void execute(MessageReceivedEvent event) {
            manageRoleMember(event, true);
        }
    },
    REMOVEROLE(" [ROLE] [@Name] (To remove someone else's role) OR [ROLE] (To remove your own role)") {
        @Override
        public void execute(MessageReceivedEvent event) {
            manageRoleMember(event, false);
        }
    },
    QUIT("Quits the bot") {
        @Override
        public void execute(MessageReceivedEvent event) {
            channel = event.getChannel();
            channel.sendMessage("QUITING!").queue();
            event.getJDA().shutdown();
        }
    },
    RCON("[ip:port] \"[command]\"") {
        @Override
        public void execute(MessageReceivedEvent event) {
            channel = event.getChannel();

            String authorAsMention = event.getAuthor().getAsMention();
            try {
                String[] fullIP = event.getMessage().getContentRaw().split("\\s");

                StringBuilder commandBuilder = new StringBuilder();
                for (int i = 2; i < fullIP.length; i++) {
                    commandBuilder.append(fullIP[i]).append(" ");
                }
                String command = commandBuilder.toString().trim();
                if (command.matches("^\"[a-zA-Z0-9\\s'_]+\"") || command.matches("[a-z_]+")) {
                    command = command.replaceAll("\"", "");
                } else {
                    channel.sendMessage(authorAsMention + ", this command is not valid.").queue();
                    return;
                }

                boolean found = false;
                String cmdName = command.split("\\s")[0];
                System.out.println(cmdName);
                for (RconCommand cmd : RconCommand.values()) {
                    if (cmdName.equals(cmd.name().toLowerCase())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    channel.sendMessage(authorAsMention + ", this command does not exist or is restricted.").queue();
                    return;
                }

                fullIP = fullIP[1].split(":");
                String host = fullIP[0];
                if (!host.matches("[0-9.]+")) {
                    channel.sendMessage(authorAsMention + ", this is not a valid IP address.").queue();
                    return;
                }
                int port = Integer.parseInt(fullIP[1]);
                byte[] password = props.getRconData().get(host + ":" + port);
                if (password == null) {
                    channel.sendMessage(authorAsMention + ", this IP is not recognized.").queue();
                    return;
                }
                Rcon rcon = new Rcon(host, port, password);
                System.out.println("Connected successfully");

                System.out.println(command);
                String message = rcon.command(command);

                channel.sendMessage(authorAsMention + ", " + message).queue();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticationException e) {
                channel.sendMessage(authorAsMention + ", " + e.getMessage()).queue();
            } catch (ArrayIndexOutOfBoundsException e) {
                channel.sendMessage(authorAsMention + ", wrong amount of arguments.").queue();
            } catch (NumberFormatException e) {
                channel.sendMessage(authorAsMention + ", this is not a valid Port.").queue();
            }
        }
    };

    private static String BAD_ROLE = ", this role does not exist.";
    Guild guild;
    GuildController guildController;
    MessageChannel channel;
    String usage;

    Command(String usage) {
        this.usage = ": " + usage;
    }

    public abstract void execute(MessageReceivedEvent event);

    private Role getRole(List<Role> roles, String[] arguments) {
        int nrRoles = 0;
        Role update = null;
        for (Role role : roles) {
            String rName = role.getName().toLowerCase();
            String argument = arguments[1].toLowerCase();
            if (rName.equals(argument)) {
                update = role;
                break;
            } else if (rName.contains(argument)) {
                update = role;
                nrRoles++;
            }
        }
        if (nrRoles > 1) {
            BAD_ROLE = ", there is more than 1 role that contains this word.";
            update = null;
        }
        return update;
    }

    void manageRoleMember(MessageReceivedEvent event, boolean addremoveRole) {
        //TODO make it possible for roles to contain spaces.
        channel = event.getChannel();
        guild = event.getGuild();
        guildController = new GuildController(guild);
        List<Role> roles = guild.getRoles();
        String[] arguments = event.getMessage().getContentRaw().split("\\s");
        String author = event.getAuthor().getAsMention();
        if (arguments.length == 2 || arguments.length == 3) {
            Role role;
            Member member;
            List<Member> mentionedMembers = event.getMessage().getMentionedMembers();
            if (mentionedMembers.size() == 1) {
                role = getRole(roles, arguments);
                member = mentionedMembers.get(0);
            } else {
                role = getRole(roles, arguments);
                member = event.getMember();
            }
            if (role != null) {
                try {
                    if (addremoveRole) {
                        guildController.addRolesToMember(member, role).queue();
                        channel.sendMessage(member.getAsMention() + "'s role was updated.").queue();
                    } else {
                        guildController.removeRolesFromMember(member, role).queue();
                        channel.sendMessage(member.getAsMention() + "'s role was removed.").queue();
                    }
                } catch (HierarchyException e) {
                    channel.sendMessage(author + ", this role is too high level to be touched.").queue();
                }
            } else {
                channel.sendMessage(author + BAD_ROLE).queue();
                BAD_ROLE = ", this role does not exist.";
            }
        } else {
            channel.sendMessage(author + ", wrong amount of arguments.").queue();
        }
    }

}
