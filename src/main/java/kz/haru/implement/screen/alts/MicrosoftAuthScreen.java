package kz.haru.implement.screen.alts;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.implement.screen.alts.auth.MicrosoftAuth;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.concurrent.CompletableFuture;

public class MicrosoftAuthScreen extends Screen {
    private final Screen parentScreen;
    private String statusMessage = "Готов к авторизации через Microsoft";
    private boolean isLoggingIn = false;
    private CompletableFuture<Alt> loginFuture;
    
    public MicrosoftAuthScreen(Screen parentScreen) {
        super(new StringTextComponent("Microsoft авторизация"));
        this.parentScreen = parentScreen;
    }
    
    @Override
    protected void init() {
        super.init();

        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + 40, 200, 20, 
            new StringTextComponent("Авторизация через Microsoft"), button -> {
                if (!isLoggingIn) {
                    startMicrosoftAuth();
                }
            }));
            
        this.addButton(new Button(this.width / 2 - 100, this.height / 2 + 90, 200, 20, 
            new StringTextComponent("Вернуться"), button -> {
                this.minecraft.displayGuiScreen(parentScreen);
            }));
    }
    
    private void startMicrosoftAuth() {
        isLoggingIn = true;
        statusMessage = "Открывается браузер для авторизации...";
        
        loginFuture = MicrosoftAuth.loginWithBrowser(status -> {
            statusMessage = status;
        });
        
        loginFuture.whenComplete((alt, ex) -> {
            if (ex != null) {
                statusMessage = "Ошибка: " + ex.getMessage();
                isLoggingIn = false;
            } else if (alt != null) {
                statusMessage = "Авторизация успешна! Добро пожаловать, " + alt.getUsername();
                isLoggingIn = false;
                
                minecraft.execute(() -> {
                    minecraft.displayGuiScreen(parentScreen);
                });
            } else {
                statusMessage = "Ожидание авторизации в браузере...";
            }
        });
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, 20, 0xFFFFFF);
        
        drawCenteredString(matrixStack, this.font, "Вход через Microsoft аккаунт", this.width / 2, 50, 0xCCCCCC);
        
        drawCenteredString(matrixStack, this.font, 
            "Нажмите кнопку ниже для авторизации. Откроется браузер,", 
            this.width / 2, 80, 0xAAAAAA);
        drawCenteredString(matrixStack, this.font, 
            "где вам нужно войти в свой аккаунт Microsoft.", 
            this.width / 2, 95, 0xAAAAAA);
        
        drawCenteredString(matrixStack, this.font, statusMessage, this.width / 2, this.height / 2 - 10, 0xFFFF55);
        
        if (isLoggingIn) {
            String dots = "";
            int time = (int)((System.currentTimeMillis() / 500) % 4);
            for (int i = 0; i < time; i++) {
                dots += ".";
            }
            
            drawCenteredString(matrixStack, this.font, "Ожидание" + dots, this.width / 2, this.height / 2 + 10, 0xFFFF55);
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void onClose() {
        if (loginFuture != null && !loginFuture.isDone()) {
            loginFuture.cancel(true);
        }
        
        super.onClose();
    }
} 