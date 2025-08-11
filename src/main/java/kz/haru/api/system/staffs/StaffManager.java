package kz.haru.api.system.staffs;

import kz.haru.client.Haru;
import kz.haru.common.config.ConfigFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaffManager {
    private static final File file = new File(Haru.getClientConfigsPath() + "/staffs.json");
    private static final Set<String> staffs = new HashSet<>();

    public static Set<String> getStaffsList() {
        return staffs;
    }

    public static List<String> getStaffNames() {
        return new ArrayList<>(staffs);
    }

    public static void addStaff(String name) {
        if (name == null || name.trim().isEmpty()) return;
        if (staffs.add(name)) {
            saveStaffs();
        }
    }

    public static void removeStaff(String name) {
        if (staffs.remove(name)) {
            saveStaffs();
        }
    }

    public static boolean isStaff(String name) {
        return staffs.contains(name);
    }

    public static void clear() {
        staffs.clear();
        saveStaffs();
    }

    public static void saveStaffs() {
        ConfigFile.saveStrings(file, staffs);
    }

    public static void loadStaffs() {
        ConfigFile.loadStrings(file, staffs);
    }
}
