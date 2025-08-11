package kz.haru.api.module;

import kz.haru.common.utils.text.fonts.Fonts;
import kz.haru.implement.modules.combat.KillAuraModule;
import kz.haru.implement.modules.combat.AutoTotemModule;
import kz.haru.implement.modules.combat.ElytraTargetModule;
import kz.haru.implement.modules.combat.NoFriendDamage;
import kz.haru.implement.modules.misc.*;
import kz.haru.implement.modules.movement.*;
import kz.haru.implement.modules.player.*;
import kz.haru.implement.modules.render.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Class<? extends Module>, Module> moduleMap = new ConcurrentHashMap<>();

    public void init() {
        registerModules(
                new FlightModule(),
                new NoSlowModule(),
                new SRPSpooferModule(),
                new TargetStrafeModule(),
                new ClickGUIModule(),
                new ElytraTargetModule(),
                new NoPushModule(),
                new FreeCameraModule(),
                new AutoTpaAcceptModule(),
                new ElytraSwapperModule(),
                new AutoTotemModule(),
                new RemovalsModule(),
                new TargetEspModule(),
                new ItemESPModule(),
                new WorldTimeModule(),
                new ClickPearlModule(),
                new AutoRespawnModule(),
                new FullBrightModule(),
                new TapeMouseModule(),
                new AutoHealModule(),
                new AutoPilotModule(),
                new NoJumpDelayModule(),
                new SuperFireworkModule(),
                new NameTagsModule(),
                new AutoLeaveModule(),
                new SprintModule(),
                new GuiMoveModule(),
                new InterfaceModule(),
                new KillAuraModule(),
                new MoveFixModule(),
                new AssistantModule(),
                new ViewModelModule(),
                new SwingAnimationModule(),
                new BaritoneModule(),
                new ClickFriendModule(),
                new NoFriendDamage()
        );

        modules.sort(Comparator.comparing(module -> -Fonts.bold.getWidth(module.getName(), 7f)));
    }

    private void registerModules(Module... modulesArray) {
        modules.addAll(Arrays.asList(modulesArray));
        for (Module module : modulesArray) {
            moduleMap.put(module.getClass(), module);
        }
    }
    
    public <T extends Module> T get(Class<T> moduleClass) {
        return moduleClass.cast(moduleMap.get(moduleClass));
    }
}