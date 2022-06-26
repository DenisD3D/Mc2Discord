package ml.denisd3d.mc2discord.forge;

import com.google.common.collect.Lists;
import net.minecraft.crash.ICrashReportDetail;

import javax.annotation.Nullable;
import java.util.List;

public class EnvCategory {
    private final EnvGenerator report;
    private final String title;
    private final List<Entry> entries = Lists.newArrayList();

    public EnvCategory(EnvGenerator p_i1353_1_, String p_i1353_2_) {
        this.report = p_i1353_1_;
        this.title = p_i1353_2_;
    }


    public EnvCategory setDetail(String p_189529_1_, ICrashReportDetail<String> p_189529_2_) {
        try {
            this.setDetail(p_189529_1_, p_189529_2_.call());
        } catch (Throwable throwable) {
            this.setDetailError(p_189529_1_, throwable);
        }

        return this;
    }

    public EnvCategory setDetail(String p_71507_1_, Object p_71507_2_) {
        this.entries.add(new Entry(p_71507_1_, p_71507_2_));
        return this;
    }

    public void setDetailError(String p_71499_1_, Throwable p_71499_2_) {
        this.setDetail(p_71499_1_, p_71499_2_);
    }

    public void getDetails(StringBuilder p_85072_1_) {
        p_85072_1_.append("-- ").append(this.title).append(" --\n");
        p_85072_1_.append("Details:");

        for (Entry crashreportcategory$entry : this.entries) {
            p_85072_1_.append("\n\t");
            p_85072_1_.append(crashreportcategory$entry.getKey());
            p_85072_1_.append(": ");
            p_85072_1_.append(crashreportcategory$entry.getValue());
        }

    }

    static class Entry {
        private final String key;
        private final String value;

        public Entry(String p_i1352_1_, @Nullable Object p_i1352_2_) {
            this.key = p_i1352_1_;
            if (p_i1352_2_ == null) {
                this.value = "~~NULL~~";
            } else if (p_i1352_2_ instanceof Throwable) {
                Throwable throwable = (Throwable) p_i1352_2_;
                this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            } else {
                this.value = p_i1352_2_.toString();
            }

        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}

