package kz.haru.implement.screen.alts;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.client.Haru;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

public class OfflineAuthScreen extends Screen {
    private final Screen parentScreen;
    private TextFieldWidget usernameField;
    private String statusMessage = "";
    
    public OfflineAuthScreen(Screen parentScreen) {
        super(new StringTextComponent("Оффлайн аккаунт"));
        this.parentScreen = parentScreen;
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.usernameField = new TextFieldWidget(this.font, this.width / 2 - 100, this.height / 2 - 10, 200, 20, new StringTextComponent("Имя пользователя"));
        this.usernameField.setMaxStringLength(16);
        this.usernameField.setFocused2(true);
        this.children.add(this.usernameField);
        
        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + 20, 200, 20, 
            new StringTextComponent("Добавить аккаунт"), button -> {
                addOfflineAccount();
            }));
    }
    
    private void addOfflineAccount() {
        String username = this.usernameField.getText();
        
        if (username == null || username.isEmpty()) {
            this.statusMessage = "Введите имя пользователя";
            return;
        }
        
        Alt alt = AltManager.getInstance().loginOffline(username);
        
        if (alt != null) {
            this.statusMessage = "Добавлен оффлайн-аккаунт: " + alt.getUsername();
            minecraft.displayGuiScreen(new Haru().getAltManagerScreen());
            AltManager.getInstance().saveAlts();
        } else {
            this.statusMessage = "Не удалось добавить аккаунт";
        }
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, 20, 0xFFFFFF);
        
        drawCenteredString(matrixStack, this.font, "Введите имя пользователя для оффлайн-аккаунта", this.width / 2, 50, 0xCCCCCC);
        
        drawString(matrixStack, this.font, "Имя пользователя:", this.width / 2 - 100, this.height / 2 - 25, 0xAAAAAA);
        this.usernameField.render(matrixStack, mouseX, mouseY, partialTicks);
        
        if (!statusMessage.isEmpty()) {
            drawCenteredString(matrixStack, this.font, statusMessage, this.width / 2, this.height / 2 + 50, 0xFFFF55);
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (this.usernameField != null) {
            this.usernameField.tick();
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.usernameField.isFocused() && keyCode == 257) {
            addOfflineAccount();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
} 