package io.github.overlordsiii.villagernames.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.overlordsiii.villagernames.VillagerNames;
import io.github.overlordsiii.villagernames.command.argument.FormattingArgumentType;
import io.github.overlordsiii.villagernames.command.argument.GolemNameArgumentType;
import io.github.overlordsiii.villagernames.command.argument.VillagerNameArgumentType;
import io.github.overlordsiii.villagernames.command.suggestion.FormattingSuggestionProvider;
import io.github.overlordsiii.villagernames.command.suggestion.GolemNameSuggestionProvider;
import io.github.overlordsiii.villagernames.command.suggestion.VillagerNameSuggestionProvider;
import io.github.overlordsiii.villagernames.config.FormattingDummy;
import io.github.overlordsiii.villagernames.config.GolemNamesConfig;
import io.github.overlordsiii.villagernames.config.VillagerGeneralConfig;
import io.github.overlordsiii.villagernames.config.VillagerNamesConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class VillagerNameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
    dispatcher.register(literal("villagername")
        .requires(source -> VillagerNames.CONFIG.villagerGeneralConfig.needsOP ? source.hasPermissionLevel(2) : source.hasPermissionLevel(4))
            .then(literal("toggle")
                .then(literal("professionNames")
                    .executes(context -> executeToggle(context, "professionNames", "Profession names are now toggled %s")))
                .then(literal("golemNames")
                    .executes(context -> executeToggle(context, "golemNames", "Golem Names are now toggled %s")))
                .then(literal("needsOP")
                    .executes(context -> executeToggle(context, "needsOP", "The VillagerNames commands that need op are now toggled %s")))
                .then(literal("childNames")
                    .executes(context -> executeToggle(context, "childNames", "Children Having names is now toggled %s")))
                .then(literal("turnOffConsoleSpam")
                    .executes(context -> executeToggle(context, "turnOffVillagerConsoleSpam", "Villagers dying spamming the console is now toggled %s")))
                .then(literal("wanderingTraderNames")
                    .executes(context -> executeToggle(context, "wanderingTraderNames", "Wandering Traders having names is now toggled %s"))))
            .then(literal("add")
                .then(literal("villagerNames")
                    .then(argument("villagerName", StringArgumentType.greedyString())
                        .executes(context -> executeAdd(context, VillagerNames.CONFIG.villagerNamesConfig.villagerNames, StringArgumentType.getString(context, "villagerName"), "Added %s to the villager names list", "villagerNames"))))
                .then(literal("golemNames")
                    .then(argument("golemName", StringArgumentType.greedyString())
                        .executes(context -> executeAdd(context, VillagerNames.CONFIG.golemNamesConfig.golemNames, StringArgumentType.getString(context, "golemName"), "Added %s to the golem names list", "golemNames")))))
            .then(literal("remove")
                .then(literal("villagerNames")
                    .then(argument("villagerNam", VillagerNameArgumentType.villagerName())
                        .suggests(new VillagerNameSuggestionProvider())
                            .executes(context -> executeRemove(context, VillagerNames.CONFIG.villagerNamesConfig.villagerNames, VillagerNameArgumentType.getVillagerName(context, "villagerNam"), "Removed %s from the villager names list", "villagerNames"))))
                .then(literal("golemNames")
                    .then(argument("golemNam", GolemNameArgumentType.golemName())
                        .suggests(new GolemNameSuggestionProvider())
                            .executes(context -> executeRemove(context, VillagerNames.CONFIG.golemNamesConfig.golemNames, GolemNameArgumentType.getGolemName(context, "golemNam"), "Removed %s from the golem names list", "golemNames")))))
            .then(literal("set")
                .then(literal("nitwitText")
                    .then(argument("nitwit", StringArgumentType.greedyString())
                        .executes(context -> executeSetString(context, "nitwitText", StringArgumentType.getString(context, "nitwit"), "The nitwit Text is now set to '%s'"))))
                .then(literal("villagerTextFormat")
                    .then(argument("format", FormattingArgumentType.format())
                        .suggests(new FormattingSuggestionProvider())
                            .executes(context -> executeSetFormatting(context, "The villager Text formatting is now set to %s", FormattingArgumentType.getFormat(context, "format")))))
                .then(literal("wanderingTraderText")
                    .then(argument("wanderingText", StringArgumentType.greedyString())
                        .executes(context -> executeSetString(context, "wanderingTraderText", StringArgumentType.getString(context, "wanderingText"), "The Wandering Trader Text is now set to '%s'")))))
            .then(literal("info")
                .executes(VillagerNameCommand::executeInfo)));
    }
    @SuppressWarnings("ALL")
    private static int executeSetFormatting(CommandContext<ServerCommandSource> ctx, String displayText, Formatting newFormatting) throws CommandSyntaxException {
        VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting = FormattingDummy.fromFormatting(newFormatting);
        ctx.getSource().sendFeedback(new LiteralText(String.format(displayText
                , FormattingDummy.fromFormatting(newFormatting)))
                .formatted(newFormatting == Formatting.OBFUSCATED ? Formatting.WHITE : newFormatting).styled(style ->
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND
                                , "/villagername set villagerTextFormat " +
                                FormattingDummy.fromFormatting(newFormatting).toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new LiteralText(FormattingDummy.fromFormatting(newFormatting)
                                                .toString()).formatted(newFormatting))))
                , true);
        VillagerNames.CONFIG_MANAGER.save();
        try {
            broadCastConfigChangeToOps(ctx, ConfigChange.SET, VillagerGeneralConfig.class.getDeclaredField("villagerTextFormatting"), ctx.getSource().getPlayer(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    private static int executeSetString(CommandContext<ServerCommandSource> ctx, String literal, String newvalue, String displayedText){
        switch (literal){
            case "nitwitText": VillagerNames.CONFIG.villagerGeneralConfig.nitwitText = newvalue;
            case "wanderingTraderText": VillagerNames.CONFIG.villagerGeneralConfig.wanderingTraderText = newvalue;
        }
        String text = String.format(displayedText, newvalue);
        ctx.getSource().sendFeedback(new LiteralText(text).formatted(Formatting.LIGHT_PURPLE)
                .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                        , new LiteralText(
                                "/villagername set " + literal + " " + newvalue)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
                                , "/villagername set " + literal))), true);
        VillagerNames.CONFIG_MANAGER.save();
        try {
            broadCastConfigChangeToOps(ctx, ConfigChange.SET, VillagerGeneralConfig.class.getDeclaredField(literal), ctx.getSource().getPlayer(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    private static int executeToggle(CommandContext<ServerCommandSource> ctx, String literal, String displayText) throws CommandSyntaxException {
        String onOrOff;
        switch (literal){
            case "professionNames":
                VillagerNames.CONFIG.villagerGeneralConfig.professionNames = !VillagerNames.CONFIG.villagerGeneralConfig.professionNames;
                if (VillagerNames.CONFIG.villagerGeneralConfig.professionNames){
                    onOrOff = "on";
                }
                else{
                    onOrOff = "off";
                }
                break;
            case "needsOP":
                VillagerNames.CONFIG.villagerGeneralConfig.needsOP = !VillagerNames.CONFIG.villagerGeneralConfig.needsOP;
                if (VillagerNames.CONFIG.villagerGeneralConfig.needsOP){
                    onOrOff = "on";
                }
                else{
                    onOrOff = "off";
                }
                break;
            case "childNames":
                VillagerNames.CONFIG.villagerGeneralConfig.childNames = !VillagerNames.CONFIG.villagerGeneralConfig.childNames;
                if (VillagerNames.CONFIG.villagerGeneralConfig.childNames){
                    onOrOff = "on";
                }
                else{
                    onOrOff = "off";
                }
                break;
            case "turnOffVillagerConsoleSpam":
                VillagerNames.CONFIG.villagerGeneralConfig.turnOffVillagerConsoleSpam = !VillagerNames.CONFIG.villagerGeneralConfig.turnOffVillagerConsoleSpam;
                if (VillagerNames.CONFIG.villagerGeneralConfig.turnOffVillagerConsoleSpam){
                    onOrOff = "on";
                }
                else{
                    onOrOff = "off";
                }
                break;
            case "golemNames":
                VillagerNames.CONFIG.villagerGeneralConfig.golemNames = !VillagerNames.CONFIG.villagerGeneralConfig.golemNames;
                if (VillagerNames.CONFIG.villagerGeneralConfig.golemNames){
                    onOrOff = "on";
                }
                else{
                    onOrOff = "off";
                }
                break;
            case "wanderingTraderNames":
                VillagerNames.CONFIG.villagerGeneralConfig.wanderingTraderNames = !VillagerNames.CONFIG.villagerGeneralConfig.wanderingTraderNames;
                if (VillagerNames.CONFIG.villagerGeneralConfig.wanderingTraderNames){
                    onOrOff = "on";
                }
                else{
                    onOrOff = "off";
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + literal);
        }

      //      System.out.println("onOrOff = " + onOrOff);
          String text = String.format(displayText, onOrOff);
          ctx.getSource().sendFeedback(
                  new LiteralText(text).formatted(Formatting.YELLOW)
                          .styled(style -> style.withClickEvent(
                                  new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
                                          , "/villagername toggle " + literal))
                                  .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                                          , new LiteralText("Toggle the " + literal + " rule")))), true);
        VillagerNames.CONFIG_MANAGER.save();
        try {
            broadCastConfigChangeToOps(ctx, ConfigChange.TOGGLE, VillagerGeneralConfig.class.getDeclaredField(literal), ctx.getSource().getPlayer(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    private static int executeAdd(CommandContext<ServerCommandSource> ctx, List<String> listToAddTo, String toAdd, String displayText, String literal) throws CommandSyntaxException {
        if (!listToAddTo.contains(toAdd)){
            listToAddTo.add(toAdd);
            String text = String.format(displayText, toAdd);
            ctx.getSource().sendFeedback(
                    new LiteralText(text).formatted(Formatting.AQUA)
                            .styled(style -> style.withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT
                                    , new LiteralText("Add an item to the villager or golem list")))
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND
                                            , "/villagername add " + toAdd))), true);
        }
        else{
            ctx.getSource().getPlayer().sendMessage(
                    new LiteralText("The villager or golem list you tried to add too already had that name in it")
                            .formatted(Formatting.RED), false);
        }
        try {
            if (literal.contains("villagerNames")) {
                broadCastConfigChangeToOps(ctx, ConfigChange.ADD, VillagerNamesConfig.class.getDeclaredField(literal), ctx.getSource().getPlayer(), toAdd);
            } else {
                broadCastConfigChangeToOps(ctx, ConfigChange.ADD, GolemNamesConfig.class.getDeclaredField(literal), ctx.getSource().getPlayer(), toAdd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        VillagerNames.CONFIG_MANAGER.save();
        return 1;
    }
    private static int executeRemove(CommandContext<ServerCommandSource> ctx, List<String> listToRemoveFrom, String toRemove, String displayText, String name) throws CommandSyntaxException {
        if (listToRemoveFrom.contains(toRemove)){
            listToRemoveFrom.remove(toRemove);
            String text = String.format(displayText, toRemove);
            ctx.getSource().sendFeedback(new LiteralText(text)
                    .formatted(Formatting.GOLD)
                    .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                            , new LiteralText("Remove a name from the villager or golem name list")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/villagername remove " + toRemove)))
                    , true);
        }
        else{
            ctx.getSource().getPlayer().sendMessage(new LiteralText("The villager or golem list you tried to remove from does not have that name").formatted(Formatting.RED), false);
        }

        try {
            if (name.contains("villagerNames")) {
                broadCastConfigChangeToOps(ctx, ConfigChange.REMOVE, VillagerNamesConfig.class.getDeclaredField(name), ctx.getSource().getPlayer(), toRemove);
            } else {
                broadCastConfigChangeToOps(ctx, ConfigChange.REMOVE, GolemNamesConfig.class.getDeclaredField(name), ctx.getSource().getPlayer(), toRemove);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        VillagerNames.CONFIG_MANAGER.save();
        return 1;
    }
    private static int executeInfo(CommandContext<ServerCommandSource> ctx){
        try {
            for (Field field : VillagerGeneralConfig.class.getDeclaredFields()) {
                ctx.getSource().getPlayer().sendMessage(
                        new LiteralText(field.getName() + " = " + field.get(VillagerNames.CONFIG.villagerGeneralConfig).toString())
                                .styled(style -> style.withClickEvent(
                                        FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ?
                                                new ClickEvent(ClickEvent.Action.OPEN_FILE, FabricLoader.getInstance().getConfigDir() + "\\VillagerNames\\" + "villagerRules.json")
                                                : new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/villagername info"))), false);


            }
        } catch (IllegalAccessException | CommandSyntaxException ex){
            ex.printStackTrace();
        }
        return 1;
    }
    private static void broadCastConfigChangeToOps(CommandContext<ServerCommandSource> ctx, ConfigChange change, Field field, ServerPlayerEntity executor, @Nullable String addedItem) throws IllegalAccessException {
        LiteralText text;
        switch (change){
            case SET:{
                text = (LiteralText) new LiteralText("The " + field.getName() + " has been set to \""
                        + field.get(VillagerNames.CONFIG.villagerGeneralConfig).toString() + "\" by "
                        + executor.getName().asString() + ".").formatted(field.getName().equals("villagerTextFormatting")
                        ? FormattingDummy.valueOf(field.get(VillagerNames.CONFIG.villagerGeneralConfig).toString()).getFormatting()
                        : Formatting.LIGHT_PURPLE);
                break;
            }
            case ADD: {
                text = (LiteralText) new LiteralText("The " + field.getName() + " has had the name \"" + addedItem + "\" added to the " + field.getName() + " by " + executor.getName().asString() + ".").formatted(Formatting.AQUA);
                break;
            }
            case TOGGLE: {
                text = (LiteralText) new LiteralText("The " + field.getName() + " has been toggled to " + field.get(VillagerNames.CONFIG.villagerGeneralConfig).toString() + " by " + executor.getName().asString() + ".").formatted(Formatting.GRAY);
                break;
            }
            case REMOVE: {
                text = (LiteralText) new LiteralText("The " + field.getName() + " has had the name \"" + addedItem + "\" removed from it by " + executor.getName().asString() + ".").formatted(Formatting.YELLOW);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + change);
        }
        addConfigText(text);
        sendToOps(ctx, text);
    }
    private static void addConfigText(LiteralText text){
        text.append(new LiteralText(" Any changes to the config require a server restart.")
                .formatted(Formatting.ITALIC, Formatting.GRAY))
                .append(new LiteralText(" Would you like to restart the server?")
                        .formatted(Formatting.BOLD, Formatting.GOLD).styled(style ->
                                style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stop"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                new LiteralText("⚠WARNING! YOU HAVE TO RESTART THE SERVER BY YOURSELF!⚠").formatted(Formatting.RED)))));
    }
    private static void sendToOps(CommandContext<ServerCommandSource> ctx, Text text){
        ctx.getSource().getMinecraftServer().getPlayerManager().getPlayerList().forEach((serverPlayerEntity -> {
            if (ctx.getSource().getMinecraftServer().getPlayerManager().isOperator(serverPlayerEntity.getGameProfile())){
                serverPlayerEntity.sendMessage(text, false);
            }
        }));
    }
    private enum ConfigChange {
        SET,
        TOGGLE,
        ADD,
        REMOVE,
    }
}
