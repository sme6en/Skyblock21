package com.skyblock21.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skyblock21.Skyblock21;
import com.skyblock21.util.TextUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.ComponentChanges;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;
import org.lwjgl.glfw.GLFW;
import com.skyblock21.mixin.accessors.ChatHudAccessor;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Debug {

    private static final Codec<RegistryEntry<Item>> EMPTY_ALLOWING_ITEM_CODEC = Registries.ITEM.getEntryCodec();
    public static final Codec<ItemStack> EMPTY_ALLOWING_ITEMSTACK_CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            EMPTY_ALLOWING_ITEM_CODEC.fieldOf("id").forGetter(ItemStack::getRegistryEntry),
            Codec.INT.orElse(1).fieldOf("count").forGetter(ItemStack::getCount),
            ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY)
                                  .forGetter(ItemStack::getComponentChanges)
    ).apply(instance, ItemStack::new)));
    private static final RegistryWrapper.WrapperLookup LOOKUP = BuiltinRegistries.createWrapperLookup();

    public static KeyBinding dumpGui = new KeyBinding("Dump GUI", GLFW.GLFW_KEY_J, "SkyBlock 21");

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof HandledScreen<?> handledScreen)) return;
            ScreenKeyboardEvents.afterKeyPress(MinecraftClient.getInstance().currentScreen)
                                .register((screen2, keyCode, scanCode, modifiers) -> {
                                    if (dumpGui.matchesKey(keyCode, scanCode)) {
                                        copyGui(screen2);
                                    }
                                });
        });

        ClientCommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess) -> {
            commandDispatcher.register(literal("debugarmorstands").executes(Debug::copyArmorStands));
        }));

        ClientCommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess) -> {
            commandDispatcher.register(literal("debugchat").executes(Debug::copyChat));
        }));
    }

    public static int copyChat(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            TextUtils.addMessage("No world loaded!", true, false);
            return 1;
        }
        List<ChatHudLine> messages = ((ChatHudAccessor) client.inGameHud.getChatHud()).getMessages().reversed();
        JsonArray jsonArray = new JsonArray();

        int startIndex = Math.max(0, messages.size() - 30);

        for (int i = startIndex; i < messages.size(); i++) {
            ChatHudLine line = messages.get(i);
            Text message = line.content();

            // Serialize the Text component to JSON string
            String jsonText = Text.Serialization.toJsonString(message, client.getNetworkHandler()
                                                                             .getRegistryManager());

            // Add the JSON string as a JSON primitive to the array
            jsonArray.add(new JsonPrimitive(jsonText));
        }
        // Copy to clipboard
        client.keyboard.setClipboard(Skyblock21.GSON.toJson(jsonArray));
        TextUtils.addMessage("Copied chat data to clipboard!", true, true);
        return 1;
    }

    public static int copyArmorStands(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            TextUtils.addMessage("No world loaded!", true, false);
            return 1;
        }

        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"armor_stands\": [\n");
        client.world.getEntitiesByClass(ArmorStandEntity.class, MinecraftClient.getInstance().player.getBoundingBox().expand(40d), EntityPredicates.NOT_MOUNTED).forEach(armorStand -> {
            json.append("    {\n");
            json.append("      \"id\": \"").append(armorStand.getUuid()).append("\",\n");
            json.append("      \"name\": \"").append(armorStand.hasCustomName() ? armorStand.getCustomName().getString() : "").append("\"\n");
            json.append("      }\n");
        });
        if (json.charAt(json.length() - 2) == ',') {
            json.deleteCharAt(json.length() - 2); // Remove last comma
        }
        json.append("  ]\n");
        json.append("}");

        // Copy to clipboard
        client.keyboard.setClipboard(json.toString());
        TextUtils.addMessage("Copied armor stands data to clipboard!", true, true);
        return 1;
    }

    public static void copyGui(Screen screen) {
        if (!(screen instanceof GenericContainerScreen)) return;

        GenericContainerScreen gui = (GenericContainerScreen) screen;
        // json object that has title, width, height, and items array with each items full nbt data
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"title\": \"").append(gui.getTitle().getString()).append("\",\n");
        json.append("  \"width\": ").append(gui.width).append(",\n");
        json.append("  \"height\": ").append(gui.height).append(",\n");
        json.append("  \"items\": [\n");
        gui.getScreenHandler().slots.forEach(slot -> {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) return;
            //slot id
            json.append("    {\n");
            json.append("      \"slot\": ").append(slot.id).append(",\n");
            json.append("      \"item\": ").append(stack.isEmpty() ? "null" : formatStack(stack)).append("\n");
            json.append("    },\n");
        });
        if (json.charAt(json.length() - 2) == ',') {
            json.deleteCharAt(json.length() - 2); // Remove last comma
        }
        json.append("  ]\n");
        json.append("}");
        // Copy to clipboard
        MinecraftClient.getInstance().keyboard.setClipboard(json.toString());
        TextUtils.addMessage("Copied GUI data to clipboard!", true, false);
    }

    public static Text formatStack(ItemStack stack) {
        return Text.literal(Skyblock21.GSON.toJson(EMPTY_ALLOWING_ITEMSTACK_CODEC.encodeStart(getRegistryWrapperLookup().getOps(JsonOps.INSTANCE), stack)
                                                                                 .getOrThrow()));
    }

    public static RegistryWrapper.WrapperLookup getRegistryWrapperLookup() {
        MinecraftClient client = MinecraftClient.getInstance();
        // Null check on client for tests
        return client != null && client.getNetworkHandler() != null && client.getNetworkHandler()
                                                                             .getRegistryManager() != null ? client.getNetworkHandler()
                                                                                                                   .getRegistryManager() : LOOKUP;
    }

}
