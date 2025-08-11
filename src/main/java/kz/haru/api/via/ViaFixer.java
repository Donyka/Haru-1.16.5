/*package kz.haru.api.via;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import kz.haru.common.interfaces.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;



@UtilityClass
public class ViaFixer implements IMinecraft {

	public static boolean reconnected, fixerJoin;
	
	public void onEvent(Event event) {
		if (event instanceof EventWorldChange && ViaFixer.fixerJoin && (Server.isHW() || Server.isFT() || Server.is("spooky")) && ViaLoadingBase.getInstance().getTargetVersion().isNewerThanOrEqualTo(ProtocolVersion.v1_17) && mc.world.getDifficulty() != Difficulty.NORMAL) {
			fixerJoin = false;
            reconnected = true;
            
			boolean flag = mc.isIntegratedServerRunning();
	        boolean flag1 = mc.isConnectedToRealms();
	        mc.world.sendQuittingDisconnectingPacket();

	        if (flag)
	        {
	        	mc.unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
	        }
	        else {
	        	mc.unloadWorld();
	        }
	        
            mc.displayGuiScreen(new ConnectingScreen(new MainMenuScreen(), mc, ConnectingScreen.IP, ConnectingScreen.PORT));
		}
	}
	
}
*/