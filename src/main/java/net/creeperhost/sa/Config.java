package net.creeperhost.sa;

import java.util.List;

/**
 * Created by covers1624 on 31/7/23.
 */
public class Config {

    public final List<String> classAllowlist;
    public final List<String> packageAllowlist;
    public final List<PatchModule> patchModules;

    public Config(List<String> classAllowlist, List<String> packageAllowlist, List<PatchModule> patchModules) {
        this.classAllowlist = classAllowlist;
        this.packageAllowlist = packageAllowlist;
        this.patchModules = patchModules;
    }

    public static class PatchModule {

        public final List<String> classesToPatch;
        public final List<String> classAllowlist;
        public final List<String> packageAllowlist;

        public PatchModule(List<String> classesToPatch, List<String> classAllowlist, List<String> packageAllowlist) {
            this.classesToPatch = classesToPatch;
            this.classAllowlist = classAllowlist;
            this.packageAllowlist = packageAllowlist;
        }
    }
}
