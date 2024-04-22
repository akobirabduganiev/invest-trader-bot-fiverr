package tech.nuqta.investtraderbotfiverr.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TelegramBotUtils {
    public static InlineKeyboardMarkup createInlineKeyboardButton(List<Map.Entry<String, String>> buttonInfos) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        for (int i = 0; i < buttonInfos.size(); i++) {
            keyboardButtonsRow1.add(createKeyboardButton(buttonInfos.get(i)));
            if ((i + 1) % 2 == 0) {
                rowList.add(new ArrayList<>(keyboardButtonsRow1));
                keyboardButtonsRow1.clear();
            }
        }
        if (!keyboardButtonsRow1.isEmpty()) {
            rowList.add(keyboardButtonsRow1);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private static InlineKeyboardButton createKeyboardButton(Map.Entry<String, String> buttonInfo) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(buttonInfo.getKey());
        inlineKeyboardButton.setCallbackData(buttonInfo.getValue());
        return inlineKeyboardButton;
    }
}
