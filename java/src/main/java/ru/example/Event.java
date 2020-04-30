package ru.example;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class EchoHandler implements AudioReceiveHandler, AudioSendHandler {

    Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    @Override
    public boolean canReceiveCombined() {
        return queue.size() < 10;
    }

    @Override
    public void handleCombinedAudio(@Nonnull CombinedAudio combinedAudio) {
        if (!combinedAudio.getUsers().isEmpty()) {
            byte[] data = combinedAudio.getAudioData(1.0);
            queue.add(data);
        }
    }

    @Override
    public boolean canProvide() {
        return !queue.isEmpty();
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        byte[] data = queue.poll();
        return data == null ? null : ByteBuffer.wrap(data);
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}

public class Event extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
//        super.onGuildMessageReceived(event);

        System.out.println(event.getAuthor() + ": " + event.getMessage().getContentRaw());

//        if (event.getAuthor().getName().equals("BorisBochkarev")) {

            if (event.getMessage().getContentRaw().contains("Hi") || event.getMessage().getContentRaw().contains("Hello")) {
                event.getChannel().sendMessage("Привет!").queue();
            } else if (event.getMessage().getContentRaw().contains("!img")) {
                event.getChannel().sendFile(new File("/Users/borisbockarev/Desktop/discord_bot/Test Doc/test_image.png")).queue();
            } else if (event.getMessage().getContentRaw().contains("!join")) {
                Member member = event.getMember();
                GuildVoiceState state = member.getVoiceState();
                VoiceChannel channel = state.getChannel();

                Guild guild = channel.getGuild();
                AudioManager audioManager = guild.getAudioManager();
                EchoHandler handler = new EchoHandler();
                audioManager.setSendingHandler(handler);
                audioManager.setReceivingHandler(handler);

                audioManager.openAudioConnection(channel);
            }

//        }
    }
}
