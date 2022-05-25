package com.ghostchu.quickshopsuite.list;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Helper {
    public static Component replaceArgs(Component component, String match, Component replace){
       return component.replaceText(TextReplacementConfig.builder().matchLiteral(match).replacement(replace).build());
    }
    public static Component replaceArgs(Component component, String match, String replace){
        return component.replaceText(TextReplacementConfig.builder().matchLiteral(match).replacement(LegacyComponentSerializer.legacySection().deserialize(replace)).build());
    }
}
