package v.akfz.aslib.test;

import v.akfz.aslib.render.color.Color;
import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.gui.widget.impl.button.ButtonWidget;
import v.akfz.aslib.gui.widget.impl.button.CheckboxWidget;
import v.akfz.aslib.gui.widget.impl.button.SliderWidget;
import v.akfz.aslib.gui.widget.impl.group.ScrollContainer;
import v.akfz.aslib.gui.widget.impl.text.TextArea;
import v.akfz.aslib.gui.widget.impl.text.TextField;
import v.akfz.aslib.gui.widget.impl.picker.ColorPickerWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TestWidgets extends Screen {

    private ScrollContainer scrollContainer;
    private ButtonWidget testButton;
    private CheckboxWidget testCheckbox;
    private SliderWidget testSlider;
    private TextField testTextField;
    private ColorPickerWidget testColorPicker;
    private ColorPickerWidget innerColorPicker;

    private Color screenBgColor = new Color(0.15, 0.15, 0.15, 1.0);

    private int scrollX = 0, scrollY = 0;

    public TestWidgets() {
        super(Component.literal("GUI Widgets Test Screen"));
    }

    @Override
    protected void init() {
        if (scrollContainer != null) {
            scrollX = scrollContainer.getScrollOffsetX();
            scrollY = scrollContainer.getScrollOffsetY();
        }

        this.clearWidgets();

        int containerX = 20;
        int containerY = 40;
        int containerWidth = (this.width / 2) - 30;
        int containerHeight = this.height - 100;

        scrollContainer = this.addRenderableWidget(new ScrollContainer(containerX, containerY, containerWidth, containerHeight));
        scrollContainer.setContentWidth(containerWidth + 160);
        scrollContainer.setContentHeight(550);
        scrollContainer.scrollTo(scrollX, scrollY);

        ButtonWidget innerButton = new ButtonWidget(10, 10, 150, 20, "Кнопка в контейнере");
        innerButton.setClickFunc((btn, mouse) -> System.out.println("Клик по внутренней кнопке: " + mouse));
        scrollContainer.addWidget(innerButton);

        CheckboxWidget innerCheckbox = new CheckboxWidget(10, 40, 150, 20, "Чекбокс в контейнере");
        innerCheckbox.setOnToggle(checked -> System.out.println("Состояние чекбокса: " + checked));
        scrollContainer.addWidget(innerCheckbox);

        SliderWidget innerSlider = new SliderWidget(10, 70, 150, 20, 0.0, 100.0, 50.0, "Громкость: ",
                value -> System.out.println("Слайдер: " + value));
        innerSlider.setStep(5.0);
        scrollContainer.addWidget(innerSlider);

        TextField innerTextField = new TextField(10, 100, 200, 20);
        innerTextField.setPlaceholder("Введите text (однострочный)...");
        innerTextField.setMaxLength(32);
        scrollContainer.addWidget(innerTextField);

        TextArea innerTextArea = new TextArea(10, 130, 300, 100);
        innerTextArea.setPlaceholder("Многострочный текст...\nCtrl + Колесико = Масштаб\nShift + Колесико = Скролл X");
        innerTextArea.setText("Первая строка\nВторая строка\nОчень длинная третья строка для проверки горизонтального скролла");
        scrollContainer.addWidget(innerTextArea);

        innerColorPicker = new ColorPickerWidget(10, 240, 200, 130, screenBgColor, color -> {
            this.screenBgColor = color;
            System.out.println("Внутренний пикер изменил цвет: RGB(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");
        }) {
            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                double correctedX = mx + scrollContainer.getScrollOffsetX() - scrollContainer.getX();
                double correctedY = my + scrollContainer.getScrollOffsetY() - scrollContainer.getY();
                return super.mouseClicked(correctedX, correctedY, button);
            }

            @Override
            public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
                double correctedX = mx + scrollContainer.getScrollOffsetX() - scrollContainer.getX();
                double correctedY = my + scrollContainer.getScrollOffsetY() - scrollContainer.getY();
                return super.mouseDragged(correctedX, correctedY, button, dx, dy);
            }
        };
        innerColorPicker.setShowLabels(false);
        scrollContainer.addWidget(innerColorPicker);

        ButtonWidget farButton = new ButtonWidget(250, 390, 120, 20, "Далекая кнопка");
        farButton.setClickFunc((btn, mouse) -> System.out.println("Успешно прокрутили и кликнули!"));
        scrollContainer.addWidget(farButton);


        int rightX = (this.width / 2) + 10;
        int rightWidth = this.width - rightX - 20;

        testButton = this.addRenderableWidget(new ButtonWidget(rightX, 40, rightWidth, 20, "Фокус на далекую кнопку"));
        testButton.setClickFunc((btn, mouse) -> {
            scrollContainer.scrollToWidget(farButton);
        });

        testCheckbox = this.addRenderableWidget(new CheckboxWidget(rightX, 70, rightWidth, 20, "Переключатель"));

        testSlider = this.addRenderableWidget(new SliderWidget(rightX, 100, rightWidth, 20, 0.5, 2.0, 1.0, "Масштаб текста: ",
                value -> innerTextArea.setTextScale(value.floatValue())));

        testTextField = this.addRenderableWidget(new TextField(rightX, 130, rightWidth, 20));
        testTextField.setPlaceholder("Глобальный фокус...");

        int pickerHeight = this.height - 160 - 20;
        testColorPicker = this.addRenderableWidget(new ColorPickerWidget(rightX, 160, rightWidth, pickerHeight, screenBgColor, color -> {
            this.screenBgColor = color;
            System.out.println("Глобальный пикер изменил цвет: RGB(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");
        }));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int argbColor = ColorUtils.rgbToArgb(
                (int) (screenBgColor.getAlpha() * 255),
                (int) (screenBgColor.getRed() * 255),
                (int) (screenBgColor.getGreen() * 255),
                (int) (screenBgColor.getBlue() * 255)
        );
        graphics.fill(0, 0, this.width, this.height, argbColor);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        int textY = scrollContainer.getY() + scrollContainer.getHeight() + 8;
        if (textY < this.height - 25) {
            graphics.drawString(this.font, "Управление контейнером:", 20, textY, 0xAAAAAA);
            graphics.drawString(this.font, "- Колесико мыши / СКМ: Скролл", 25, textY + 12, 0x888888);
            graphics.drawString(this.font, "- Shift + Колесико: Горизонт. скролл", 25, textY + 24, 0x888888);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}