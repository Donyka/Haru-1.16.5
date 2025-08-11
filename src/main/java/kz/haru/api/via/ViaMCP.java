package kz.haru.api.via;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import kz.haru.client.ClientInfo;
import kz.haru.common.interfaces.IMinecraft;
import kz.haru.api.via.ui.VersionSelectorElement;
import net.minecraft.util.text.ITextComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;




public class ViaMCP implements IMinecraft {
	public int NATIVE_VERSION = 754;
    public List<ProtocolVersion> PROTOCOLS;
    public VersionSelectorElement viaScreen;
    

    
    private void nativka56() {
    	List<ProtocolVersion> protocolList = ProtocolVersion.getProtocols().stream().filter(pv -> pv.getVersion() == 47 || pv.getVersion() >= 107).sorted((f, s) -> Integer.compare(s.getVersion(), f.getVersion())).toList();
        this.PROTOCOLS = new ArrayList<ProtocolVersion>(protocolList.size() + 1);
        this.PROTOCOLS.addAll(protocolList);
        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(new File(ClientInfo.clientName.toLowerCase() + "/assets")).nativeVersion(754).build();
        this.viaScreen = new VersionSelectorElement(mc.fontRenderer, 5,5,100,20, ITextComponent.getTextComponentOrEmpty("1.16.5"));
    }
    
    public ViaMCP() {
    	nativka56();
    }
    
    public int getNATIVE_VERSION() {
        return this.NATIVE_VERSION;
    }

    public List<ProtocolVersion> getPROTOCOLS() {
        return this.PROTOCOLS;
    }

    public VersionSelectorElement getViaScreen() {
        return this.viaScreen;
    }
}
