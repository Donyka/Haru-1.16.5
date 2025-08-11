package kz.haru.api.system.friends;

import kz.haru.client.Haru;
import kz.haru.common.config.ConfigFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendManager {
    private static final File file = new File(Haru.getClientConfigsPath() + "/friends.json");
    private static final Set<String> friends = new HashSet<>();

    public static Set<String> getFriendList() {
        return friends;
    }

    public static List<String> getFriendNames() {
        return new ArrayList<>(friends);
    }

    public static void addFriend(String name) {
        if (name == null || name.trim().isEmpty()) return;
        if (friends.add(name)) {
            saveFriends();
        }
    }

    public static void removeFriend(String name) {
        if (friends.remove(name)) {
            saveFriends();
        }
    }

    public static boolean isFriend(String name) {
        return friends.contains(name);
    }

    public static void clear() {
        friends.clear();
        saveFriends();
    }

    public static void saveFriends() {
        ConfigFile.saveStrings(file, friends);
    }

    public static void loadFriends() {
        ConfigFile.loadStrings(file, friends);
    }
}
