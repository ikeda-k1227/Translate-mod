package jp.kotoba.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class KotobaClient implements ClientModInitializer {
    private static KotobaConfig config;
    private static Translator translator;
    private static KeyMapping toggleDisplayKey;

    @Override
    public void onInitializeClient() {
        config = KotobaConfig.load();
        translator = new Translator(config);
        toggleDisplayKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.kotoba_client.toggle_display", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8,
                KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleDisplayKey.consumeClick()) {
                config.toggleDisplayMode();
                if (client.player != null) {
                    String mode = config.showTranslations ? "翻訳表示" : "原文表示";
                    client.player.displayClientMessage(Component.literal("§8[Kotoba] §f表示を" + mode + "に切り替えました"), true);
                }
            }
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!shouldTranslate(config.enabled && config.chatEnabled, message)) return true;
            replaceWithTranslation(message, false);
            return false;
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!shouldTranslate(config.enabled && config.gameMessagesEnabled, message)) return true;
            replaceWithTranslation(message, overlay);
            return false;
        });
    }

    static Component translatedComponent(Component original, boolean categoryEnabled) {
        if (config == null || translator == null) return original;
        if (!config.enabled || !categoryEnabled || !config.showTranslations) return original;
        String text = original.getString().strip();
        if (!isTranslatable(text)) return original;
        String cached = translator.getCached(text);
        if (cached != null) return Component.literal(cached);
        translator.translate(text);
        return original;
    }

    public static Component translateScoreboard(Component original) {
        if (config == null) return original;
        return translatedComponent(original, config.scoreboardEnabled);
    }

    public static Component translateHologram(Component original) {
        if (config == null) return original;
        return translatedComponent(original, config.hologramsEnabled);
    }

    private static boolean shouldTranslate(boolean categoryEnabled, Component message) {
        return categoryEnabled && config.showTranslations && isTranslatable(message.getString().strip());
    }

    private static boolean isTranslatable(String text) {
        return !text.isEmpty() && text.length() <= config.maxCharacters && !containsJapanese(text);
    }

    private static void replaceWithTranslation(Component original, boolean overlay) {
        String text = original.getString().strip();
        translator.translate(text).thenAccept(translated -> {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                if (client.player == null) return;
                Component shown = Component.literal(translated == null || translated.isBlank() ? original.getString() : translated);
                if (overlay) client.player.sendOverlayMessage(shown);
                else client.player.sendSystemMessage(shown);
            });
        });
    }

    private static boolean containsJapanese(String text) {
        return text.codePoints().anyMatch(c -> (c >= 0x3040 && c <= 0x30ff) || (c >= 0x3400 && c <= 0x9fff));
    }
}
