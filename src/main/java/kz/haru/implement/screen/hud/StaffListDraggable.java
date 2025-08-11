package kz.haru.implement.screen.hud;

import kz.haru.api.system.staffs.StaffManager;
import kz.haru.client.functions.UpdateFunctions;
import kz.haru.common.utils.animation.AnimationUtil;
import kz.haru.common.utils.animation.Easing;
import kz.haru.common.utils.color.ColorUtil;
import kz.haru.common.utils.draggable.Draggable;
import kz.haru.common.utils.render.RenderUtil;
import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.events.render.Render2DEvent;
import kz.haru.implement.screen.hud.interfaces.ElementDraggable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;

import java.util.*;
import java.util.regex.Pattern;

public class StaffListDraggable extends ElementDraggable {
    public StaffListDraggable(Draggable draggable) {
        super(draggable);
    }

    private final Pattern prefixPattern = Pattern.compile(".*(mod|der|adm|wne|мод|хелп|помо|адм|владе|отри|таф|taf|curat|курато|dev|раз|supp|сапп|yt|юту).*");
    private final Pattern validUserPatern = Pattern.compile("^\\w{3,16}$");
    private final List<Staff> staffList = new ArrayList<>();
    private final AnimationUtil globalAnimation = new AnimationUtil(Easing.EXPO_OUT, 300);
    private final AnimationUtil heightAnimation = new AnimationUtil(Easing.EXPO_OUT, 100);
    private final AnimationUtil widthAnimation = new AnimationUtil(Easing.EXPO_OUT, 100);

    @Override
    public void update() {
        super.update();

        if (Minecraft.getInstance().gameSettings.showDebugInfo) return;

        staffList.clear();

        List<ScorePlayerTeam> sortedTeams = Minecraft.getInstance().world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList();

        for (ScorePlayerTeam team : sortedTeams) {
            String name = team.getMembershipCollection().toString();
            name = name.substring(1, name.length() - 1);

            if (validUserPatern.matcher(name).matches()) {
                boolean vanish = true;
                for (NetworkPlayerInfo info : Minecraft.getInstance().getConnection().getPlayerInfoMap()) {
                    if (info.getGameProfile().getName().equals(name)) {
                        vanish = false;
                    }
                }
                if (vanish) {
                    Staff staff = new Staff(team.getPrefix(), name, StaffStatus.Spec);
                    staffList.add(staff);
                }

                String prefix = rebuildString(team.getPrefix().getString().toLowerCase(Locale.ROOT));
                if ((prefixPattern.matcher(prefix).matches() || StaffManager.isStaff(name)) && !vanish) {
                    Staff staff = new Staff(team.getPrefix(), name, StaffStatus.Online);
                    staffList.add(staff);
                }
            }
        }
    }

    @Override
    public void render(Render2DEvent event) {
        super.render(event);

        boolean show = Minecraft.getInstance().currentScreen instanceof ChatScreen || !staffList.isEmpty();

        String name = "Staff List";
        float scale = UpdateFunctions.getInstance().getScaleFactor();
        float fontSize = 7.5f * scale;
        float nameWidth = Fonts.bold.getWidth(name, fontSize);
        float gap = 3f * scale;
        float width = nameWidth + gap * 4f;
        float height = fontSize + gap * 2f;
        float x = getDraggable().getX();
        float y = getDraggable().getY();

        RenderUtil.drawElementClientRect(x, y, (float) widthAnimation.getValue(), (float) heightAnimation.getValue(), (float) globalAnimation.getValue(), "");
        Fonts.bold.drawCenteredText(event.getMatrixStack(), name, (float) (x + widthAnimation.getValue() / 2f), y + fontSize / 2f, ColorUtil.rgb(255, 255, 255, (int) (255 * globalAnimation.getValue())), fontSize);

        y += fontSize + gap;
        height += gap;

        for (Staff staff : staffList) {
            float animProgress = 1f;

            boolean daun = staff.status == StaffStatus.Spec;
            float prefixWidth = Fonts.bold.getWidth(staff.iTextComponent, fontSize);
            float spaceWidth = Fonts.bold.getWidth(" ", fontSize);
            float staffWidth = Fonts.bold.getWidth(staff.name, fontSize);
            float specWidth = daun ? Fonts.bold.getWidth("Spec", fontSize) : 0f;
            float localWidth = prefixWidth + spaceWidth + staffWidth + gap * (daun ? 4f : 3f) + specWidth;

            if (localWidth > width) {
                width = localWidth;
            }

            Fonts.bold.drawText(event.getMatrixStack(), staff.iTextComponent, x + gap, y + fontSize / 2f, fontSize, (int) (255 * animProgress * globalAnimation.getValue()));
            Fonts.bold.drawText(event.getMatrixStack(), staff.name, x + gap + prefixWidth + spaceWidth, y + fontSize / 2f, ColorUtil.rgb(255, 255, 255, (int) (255 * animProgress * globalAnimation.getValue())), fontSize);

            if (daun) {
                Fonts.bold.drawText(event.getMatrixStack(), "Spec", x - gap + (float) widthAnimation.getValue() - specWidth, y + fontSize / 2f - 0.5f * scale, ColorUtil.rgb(255, 100, 100, (int) (255 * animProgress * globalAnimation.getValue())), fontSize);
            }

            y += fontSize * animProgress;
            height += fontSize * animProgress;
        }

        heightAnimation.run(height);
        widthAnimation.run(width);
        globalAnimation.run(show ? 1.0 : 0.0);
        getDraggable().setWidth(width);
        getDraggable().setHeight(height);
    }

    private String rebuildString(String s) {
        StringBuilder stringBuilder = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c >= 65281 && c <= 65374) {
                stringBuilder.append((char) (c - 65248));
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    @Getter
    @AllArgsConstructor
    private static class Staff {
        final ITextComponent iTextComponent;
        final String name;
        final StaffStatus status;
    }

    private enum StaffStatus {
        Online, Spec, Near
    }
}
