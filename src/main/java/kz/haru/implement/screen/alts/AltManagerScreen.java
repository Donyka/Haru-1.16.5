package kz.haru.implement.screen.alts;

import com.mojang.blaze3d.matrix.MatrixStack;
import kz.haru.client.Haru;
import kz.haru.common.utils.client.RandomNickName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class AltManagerScreen extends Screen {
    private AltList altList;
    private Button loginButton;
    private Button deleteButton;
    private String statusMessage = "";
    private Alt selectedAlt;

    private Screen parentScreen;
    private boolean showingAddMenu = false;

    public AltManagerScreen(ITextComponent titleIn) {
        super(titleIn);
    }

    @Override
    protected void init() {
        super.init();

        this.buttons.clear();
        this.children.removeIf(c -> c instanceof Button);

        if (!showingAddMenu) {
            this.altList = new AltList(this.minecraft, this.width, this.height, 32, this.height - 64, 25);
            this.children.add(this.altList);

            initMainButtons();
            loadAlts();
        } else {
            initAddMenuButtons();
        }
    }

    private void initMainButtons() {
        int centerX = this.width / 2;

        this.loginButton = this.addButton(new Button(centerX - 155, this.height - 30, 70, 20,
                new StringTextComponent("Войти"), button -> {
            if (this.selectedAlt != null) {
                loginToSelected();
            }
        }));

        this.deleteButton = this.addButton(new Button(centerX - 75, this.height - 30, 70, 20,
                new StringTextComponent("Удалить"), button -> {
            if (this.selectedAlt != null) {
                removeSelected();
            }
        }));

        this.addButton(new Button(centerX + 5, this.height - 30, 70, 20,
                new StringTextComponent("Добавить"), button -> {
            showAddMenu();
        }));

        this.addButton(new Button(centerX + 80, this.height - 30, 70, 20,
                new StringTextComponent("Вернутся"), button -> {
            minecraft.displayGuiScreen(new MainMenuScreen());
        }));

        this.addButton(new Button(centerX - 75, this.height - 55, 150, 20,
                new StringTextComponent("Рандомный"), button -> {
            addRandom();
        }));

        updateButtons();
    }


    private void addRandom(){
        String username = RandomNickName.getRandomNick();

        AltManager.getInstance().loginOffline(username);
        AltManager.getInstance().saveAlts();
        loadAlts();
        this.statusMessage = "Вход выполнен как: " + username;
    }

    private void showAddMenu() {
        showingAddMenu = true;
        this.buttons.clear();
        this.children.clear();
        initAddMenuButtons();
    }

    private void hideAddMenu() {
        showingAddMenu = false;
        this.buttons.clear();
        this.children.clear();
        init();
    }

    private void initAddMenuButtons() {
        int centerX = this.width / 2;
        int popupHeight = 150;
        int top = (this.height - popupHeight) / 2;

        this.addButton(new Button(centerX - 75, top + 40, 150, 20,
                new StringTextComponent("Microsoft аккаунт"), button -> {
            minecraft.displayGuiScreen(new MicrosoftAuthScreen(this));
        }));

        this.addButton(new Button(centerX - 75, top + 70, 150, 20,
                new StringTextComponent("Оффлайн аккаунт"), button -> {
            minecraft.displayGuiScreen(new OfflineAuthScreen(this));
        }));

        this.addButton(new Button(centerX - 75, top + 100, 150, 20,
                new StringTextComponent("Отмена"), button -> {
            hideAddMenu();
        }));
    }

    private void loadAlts() {
        if (this.altList != null) {
            List<Alt> alts = AltManager.getInstance().getAlts();
            this.altList.clearEntries();

            for (Alt alt : alts) {
                this.altList.addEntry(new AltEntry(alt));
            }
        }
    }

    private void loginToSelected() {
        boolean result = AltManager.getInstance().loginToAlt(this.selectedAlt);

        if (result) {
            this.statusMessage = "Вход выполнен как " + this.selectedAlt.getUsername();
        } else {
            this.statusMessage = "Не удалось войти в аккаунт";
        }
    }

    private void removeSelected() {
        int index = AltManager.getInstance().getAlts().indexOf(this.selectedAlt);

        if (index >= 0) {
            AltManager.getInstance().removeAlt(index);
            AltManager.getInstance().saveAlts();
            loadAlts();
            this.selectedAlt = null;
            updateButtons();
            this.statusMessage = "Аккаунт удален";
        }
    }

    private void updateButtons() {
        if (loginButton != null && deleteButton != null) {
            boolean hasSelected = this.selectedAlt != null;
            this.loginButton.active = hasSelected;
            this.deleteButton.active = hasSelected;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        int centerX = this.width / 2;

        if (!showingAddMenu) {
            if (this.altList != null) {
                // Отрисовка фона под списком
                int listLeft = this.width / 2 - 120;
                int listRight = this.width / 2 + 120;
                int listTop = 32;
                int listBottom = this.height - 64;

                fill(matrixStack, listLeft, listTop, listRight, listBottom, 0xA0202020);

                this.altList.render(matrixStack, mouseX, mouseY, partialTicks);
            }


            drawCenteredString(matrixStack, this.font, this.title.getString(), centerX, 8, 0xFFFFFF);
            drawCenteredString(matrixStack, this.font, statusMessage, centerX, 20, 0xFFFF55);

            if (this.selectedAlt != null) {
                String info = this.selectedAlt.getUsername();
                if (this.selectedAlt.isMicrosoft()) {
                    info += " (Microsoft)";
                    if (this.selectedAlt.isExpired()) {
                        info += " [Токен истек]";
                    }
                } else if (this.selectedAlt.isOffline()) {
                    info += " (Оффлайн)";
                }

            }
        } else {
            fill(matrixStack, 0, 0, this.width, this.height, 0x80000000);

            int popupWidth = 250;
            int popupHeight = 150;
            int left = (this.width - popupWidth) / 2;
            int top = (this.height - popupHeight) / 2;

            fill(matrixStack, left, top, left + popupWidth, top + popupHeight, 0xFF000000);
            fill(matrixStack, left + 1, top + 1, left + popupWidth - 1, top + popupHeight - 1, 0xFF202020);

            drawCenteredString(matrixStack, this.font, "Выберите тип аккаунта", centerX, top + 10, 0xFFFFFF);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private class AltList extends ExtendedList<AltEntry> {
        public AltList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }
    }

    private class AltEntry extends ExtendedList.AbstractListEntry<AltEntry> {
        private final Alt alt;

        public AltEntry(Alt alt) {
            this.alt = alt;
        }

        @Override
        public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            String name = this.alt.toString();
            int textWidth = AltManagerScreen.this.font.getStringWidth(name);
            int centerX = left + width / 2;
            int textLeft = centerX - textWidth / 2;

            int bgColor;

            if (AltManagerScreen.this.selectedAlt == this.alt) {
                bgColor = 0xA0808080;
            } else if (isMouseOver) {
                bgColor = 0x60404040;
            } else {
                bgColor = 0x99202020;
            }

            fill(matrixStack, textLeft - 20, top + 1, textLeft + textWidth + 25, top + height + 3, bgColor);

            drawCenteredString(matrixStack, AltManagerScreen.this.font, name, centerX, top + 8, 0xFFFFFF);
        }


        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            AltManagerScreen.this.selectedAlt = this.alt;
            AltManagerScreen.this.updateButtons();
            return true;
        }
    }
}
