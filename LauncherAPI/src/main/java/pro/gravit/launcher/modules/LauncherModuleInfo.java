package pro.gravit.launcher.modules;

import pro.gravit.utils.Version;

public class LauncherModuleInfo {
    public final String name;
    public final Version version;
    public final int priority;
    public final String[] dependencies;

    public LauncherModuleInfo(String name, Version version) {
        this.name = name;
        this.version = version;
        this.priority = 0;
        this.dependencies = new String[]{};
    }

    public LauncherModuleInfo(String name) {
        this.name = name;
        this.version = new Version(1,0,0);
        this.priority = 0;
        this.dependencies = new String[]{};
    }

    public LauncherModuleInfo(String name, Version version, String[] dependencies) {
        this.name = name;
        this.version = version;
        this.priority = 0;
        this.dependencies = dependencies;
    }

    public LauncherModuleInfo(String name, Version version, int priority, String[] dependencies) {
        this.name = name;
        this.version = version;
        this.priority = priority;
        this.dependencies = dependencies;
    }
}