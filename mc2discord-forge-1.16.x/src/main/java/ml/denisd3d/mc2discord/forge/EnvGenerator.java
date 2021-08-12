package ml.denisd3d.mc2discord.forge;

import com.google.common.collect.Lists;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.ICrashCallable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EnvGenerator {
    public static List<ICrashCallable> crashCallables = Collections.synchronizedList(new ArrayList<>());

    private final String title;
    private final EnvCategory systemDetails = new EnvCategory(this, "System Details");
    private final List<EnvCategory> details = Lists.newArrayList();

    public EnvGenerator(String p_i1348_1_) {
        this.title = p_i1348_1_;
        this.initDetails();
    }

    private static String getErrorComment() {
        String[] messages = new String[]{"Who set us up the TNT?", "Everything's going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I'm sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don't be sad. I'll do better next time, I promise!", "Don't be sad, have a hug! <3", "I just don't know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn't worry myself about that.", "I bet Cylons wouldn't have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I'm Minecraft, and I'm a crashaholic.", "Ooh. Shiny.", "This doesn't make any sense!", "Why is it breaking :(", "Don't do that.", "Ouch. That hurt :(", "You're mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!", "But it works on my machine."};

        try {
            return messages[(int) (Util.getNanos() % (long) messages.length)];
        } catch (Throwable throwable) {
            return "Witty comment unavailable :(";
        }
    }

    @SuppressWarnings("unchecked")
    private void initDetails() {
        this.systemDetails.setDetail("Minecraft Version", () -> SharedConstants.getCurrentVersion().getName());
        this.systemDetails.setDetail("Minecraft Version ID", () -> SharedConstants.getCurrentVersion().getId());
        this.systemDetails.setDetail("Operating System", () -> System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
        this.systemDetails.setDetail("Java Version", () -> System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        this.systemDetails.setDetail("Java VM Version", () -> System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        this.systemDetails.setDetail("Memory", () -> {
            Runtime runtime = Runtime.getRuntime();
            long i = runtime.maxMemory();
            long j = runtime.totalMemory();
            long k = runtime.freeMemory();
            long l = i / 1024L / 1024L;
            long i1 = j / 1024L / 1024L;
            long j1 = k / 1024L / 1024L;
            return k + " bytes (" + j1 + " MB) / " + j + " bytes (" + i1 + " MB) up to " + i + " bytes (" + l + " MB)";
        });
        this.systemDetails.setDetail("CPUs", Runtime.getRuntime().availableProcessors());
        this.systemDetails.setDetail("JVM Flags", () -> {
            List<String> list = Util.getVmArguments().collect(Collectors.toList());
            return String.format("%d total; %s", list.size(), String.join(" ", list));
        });

        for (final ICrashCallable call : crashCallables) {
            if (!call.getLabel().equals("Crash Report UUID"))
                this.systemDetails.setDetail(call.getLabel(), call);
        }
    }

    public void getDetails(StringBuilder p_71506_1_) {
        for (EnvCategory envCategory : this.details) {
            envCategory.getDetails(p_71506_1_);
            p_71506_1_.append("\n\n");
        }

        this.systemDetails.getDetails(p_71506_1_);
    }

    public String getFriendlyReport() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("---- Minecraft Crash Report ----\n");
        stringbuilder.append("// ");
        stringbuilder.append(getErrorComment());
        stringbuilder.append("\n\n");
        stringbuilder.append("Time: ");
        stringbuilder.append((new SimpleDateFormat()).format(new Date()));
        stringbuilder.append("\n");
        stringbuilder.append("Description: ");
        stringbuilder.append(this.title);
        stringbuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for (int i = 0; i < 87; ++i) {
            stringbuilder.append("-");
        }

        stringbuilder.append("\n\n");
        this.getDetails(stringbuilder);
        return stringbuilder.toString();
    }

    public EnvCategory addCategory(String p_85057_1_, int p_85057_2_) {
        EnvCategory envCategory = new EnvCategory(this, p_85057_1_);

        this.details.add(envCategory);
        return envCategory;
    }
}
