package tech.nuqta.investtraderbotfiverr.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TelegramBotUtils {
    // Initialize InlineKeyboardButton
    private static InlineKeyboardButton initializeInlineKeyboardButton(Map.Entry<String, String> buttonInfo) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(buttonInfo.getKey());
        inlineKeyboardButton.setCallbackData(buttonInfo.getValue());

        return inlineKeyboardButton;
    }

    // Create keyboard button
    private static InlineKeyboardButton createKeyboardButton(Map.Entry<String, String> buttonInfo) {
        return initializeInlineKeyboardButton(buttonInfo);
    }

    public static InlineKeyboardMarkup createInlineKeyboardButton(List<Map.Entry<String, String>> buttonInfos) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> currentRowButtons = new ArrayList<>();
        for (int i = 0; i < buttonInfos.size(); i++) {
            currentRowButtons.add(createKeyboardButton(buttonInfos.get(i)));
            if ((i + 1) % 2 == 0) {
                rowList.add(new ArrayList<>(currentRowButtons));
                currentRowButtons.clear();
            }
        }
        if (!currentRowButtons.isEmpty()) {
            rowList.add(currentRowButtons);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup createInlineKeyboardButtonOneEachRow(List<Map.Entry<String, String>> buttonInfos) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (Map.Entry<String, String> buttonInfo : buttonInfos) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            keyboardButtonsRow.add(createKeyboardButton(buttonInfo));
            rowList.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup createInlineKeyboardButtonWithLink(String text, String url) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(text);
        inlineKeyboardButton.setUrl(url);
        keyboardButtonsRow.add(inlineKeyboardButton);
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}