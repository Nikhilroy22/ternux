package com.npuja.nikhil;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANSIParser {

    public static SpannableString parse(String input) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        Pattern ansiPattern = Pattern.compile("\u001B\\[([0-9;]+)m");
        Matcher matcher = ansiPattern.matcher(input);

        int lastEnd = 0;
        int currentColor = Color.WHITE;

        while (matcher.find()) {
            String textChunk = input.substring(lastEnd, matcher.start());
            if (!textChunk.isEmpty()) {
                int start = builder.length();
                builder.append(textChunk);
                builder.setSpan(new ForegroundColorSpan(currentColor), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            String code = matcher.group(1);
            currentColor = getColorFromAnsiCode(code);
            lastEnd = matcher.end();
        }

        if (lastEnd < input.length()) {
            String remaining = input.substring(lastEnd);
            int start = builder.length();
            builder.append(remaining);
            builder.setSpan(new ForegroundColorSpan(currentColor), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return SpannableString.valueOf(builder);
    }

    private static int getColorFromAnsiCode(String code) {
    String[] codes = code.split(";");
    boolean isBold = false;
    int colorCode = 37; // default white

    for (String c : codes) {
        switch (c) {
            case "0":  isBold = false; colorCode = 37; break;
            case "1":  isBold = true; break;
            case "30": case "90": colorCode = 30; break;
            case "31": case "91": colorCode = 31; break;
            case "32": case "92": colorCode = 32; break;
            case "33": case "93": colorCode = 33; break;
            case "34": case "94": colorCode = 34; break;
            case "35": case "95": colorCode = 35; break;
            case "36": case "96": colorCode = 36; break;
            case "37": case "97": colorCode = 37; break;
        }
    }

    return getAndroidColor(colorCode, isBold);
}

private static int getAndroidColor(int code, boolean bold) {
    switch (code) {
        case 30: return Color.DKGRAY;
        case 31: return bold ? Color.RED : 0xFF800000;
        case 32: return bold ? Color.GREEN : 0xFF008000;
        case 33: return bold ? Color.YELLOW : 0xFF808000;
        case 34: return bold ? Color.BLUE : 0xFF000080;
        case 35: return bold ? Color.MAGENTA : 0xFF800080;
        case 36: return bold ? Color.CYAN : 0xFF008080;
        case 37: return bold ? Color.WHITE : 0xFFAAAAAA;
        default: return Color.WHITE;
    }
}
}