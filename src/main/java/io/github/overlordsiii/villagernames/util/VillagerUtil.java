package io.github.overlordsiii.villagernames.util;

import io.github.overlordsiii.villagernames.VillagerNames;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;



public class VillagerUtil {
    private static ArrayList<String> usedUpNames = new ArrayList<>();
    private static String upperFirstLetter(String string){
        StringBuilder builder = new StringBuilder(string);
        builder.setCharAt(0, Character.toUpperCase(string.charAt(0)));
        return builder.toString();
    }
    public static void createVillagerNames(VillagerEntity entity){
        if (!entity.hasCustomName()){
            String randomName = generateRandomVillagerName();
            if (entity.getVillagerData().getProfession() != VillagerProfession.NONE && VillagerNames.CONFIG.villagerGeneralConfig.professionNames){
                String text =  VillagerProfession.NITWIT == entity.getVillagerData().getProfession() ? VillagerNames.CONFIG.villagerGeneralConfig.nitwitText : upperFirstLetter(entity.getVillagerData().getProfession().toString());
                    entity.setCustomName(new LiteralText(randomName + " the " + text).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
                    entity.setCustomNameVisible(true);

            }
            else {
                entity.setCustomNameVisible(true);
                if (VillagerNames.CONFIG.villagerGeneralConfig.childNames) {
                    entity.setCustomName(entity.isBaby() ? new LiteralText(randomName + " the Child").formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()) : new LiteralText(randomName).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
                }
                else{
                    entity.setCustomName(new LiteralText(randomName).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
                }
            }
        }
        entity.setCustomNameVisible(true);
    }
    private static String generateRandomVillagerName(){
        Random random = new Random();
       int index = random.nextInt(VillagerNames.CONFIG.villagerNamesConfig.villagerNames.size());
       if (usedUpNames.size() > VillagerNames.CONFIG.villagerNamesConfig.villagerNames.size()/2){
           usedUpNames.clear();
       }
       if (usedUpNames.contains(VillagerNames.CONFIG.villagerNamesConfig.villagerNames.get(index))) {
           index = random.nextInt(VillagerNames.CONFIG.villagerNamesConfig.villagerNames.size());
       }
           usedUpNames.add(VillagerNames.CONFIG.villagerNamesConfig.villagerNames.get(index));
        return VillagerNames.CONFIG.villagerNamesConfig.villagerNames.get(index);
    }
    private static String generateRandomGolemName(){
        Random random = new Random();
        int index = random.nextInt(VillagerNames.CONFIG.golemNamesConfig.golemNames.size());
        if (usedUpNames.size() > VillagerNames.CONFIG.golemNamesConfig.golemNames.size()/2){
            usedUpNames.clear();
        }
        if (usedUpNames.contains(VillagerNames.CONFIG.golemNamesConfig.golemNames.get(index))) {
            index = random.nextInt(VillagerNames.CONFIG.golemNamesConfig.golemNames.size());
        }
        usedUpNames.add(VillagerNames.CONFIG.golemNamesConfig.golemNames.get(index));
        return VillagerNames.CONFIG.golemNamesConfig.golemNames.get(index);
    }
    public static void loadGolemNames(IronGolemEntity entity){
        if (!entity.hasCustomName() && VillagerNames.CONFIG.villagerGeneralConfig.golemNames) {
            String name = generateRandomGolemName();
            entity.setCustomName(new LiteralText(name).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            entity.setCustomNameVisible(true);
        }
    }
    public static void updateVillagerNames(VillagerEntity entity){
        if (entity.getVillagerData().getProfession() == VillagerProfession.NONE) {
            String random = generateRandomVillagerName();
            if (VillagerNames.CONFIG.villagerGeneralConfig.childNames) {
                entity.setCustomName(entity.isBaby() ? new LiteralText(random + " the Child").formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()) : new LiteralText(random).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            }
            else{
                entity.setCustomName(new LiteralText(random).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            }
        }
        else {
            if (!Objects.requireNonNull(entity.getCustomName()).asString().contains("the") && VillagerNames.CONFIG.villagerGeneralConfig.professionNames) {
                //LOGGER.info(entity.getCustomName().asString());
                entity.setCustomName(new LiteralText(Objects.requireNonNull(entity.getCustomName()).asString() + " the " + upperFirstLetter(entity.getVillagerData().getProfession().toString())).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
                //  LOGGER.info(entity.getCustomName().asString());
            }
        }
        entity.setCustomNameVisible(true);
    }
    public static void updateLostVillagerProfessionName(VillagerEntity entity){
        if (entity.hasCustomName() && Objects.requireNonNull(entity.getCustomName()).asString().contains(" ")) {
            String string = Objects.requireNonNull(entity.getCustomName()).asString();
            //    LOGGER.info("Custom name inside lost Villager Name = " + string);
            String realString = VillagerNames.CONFIG.villagerGeneralConfig.professionNames ? string.substring(0, string.indexOf(" ")) : string;
            //     LOGGER.info("Next string = " + realString);
            entity.setCustomName(new LiteralText(realString).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
        }
    }
    public static void updateGrownUpVillagerName(VillagerEntity entity){
        if (entity.hasCustomName()) {
            if (Objects.requireNonNull(entity.getCustomName()).asString().contains(" the")) {
                entity.setCustomName(new LiteralText(entity.getCustomName().asString().substring(0, entity.getCustomName().asString().indexOf(" "))).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            }
        }
    }
    public static void addZombieVillagerName(VillagerEntity villagerEntity, ZombieVillagerEntity zombieVillagerEntity){
   //     LOGGER.info(villagerEntity.hasCustomName());
        if (villagerEntity.hasCustomName()){
        //    LOGGER.info("before name = " + villagerEntity.getCustomName().asString());
            if (Objects.requireNonNull(villagerEntity.getCustomName()).asString().contains(" the")) {
                String string = villagerEntity.getCustomName().asString().substring(0, villagerEntity.getCustomName().asString().indexOf(" "));
           //     LOGGER.info("string = " + string);
                zombieVillagerEntity.setCustomName(new LiteralText(string + " the Zombie").formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
        //        LOGGER.info("zombie name = " + zombieVillagerEntity.getCustomName().asString());
            }
            else{
                String string = " the Zombie";
                zombieVillagerEntity.setCustomName(new LiteralText(villagerEntity.getCustomName().asString() + string).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            }
        }
    }
    public static void removeZombieVillagerName(VillagerEntity villagerEntity, ZombieVillagerEntity zombieVillagerEntity){
        if (zombieVillagerEntity.hasCustomName()){
            if (Objects.requireNonNull(zombieVillagerEntity.getCustomName()).asString().contains(" the")){
                String name = zombieVillagerEntity.getCustomName().asString().substring(0, zombieVillagerEntity.getCustomName().asString().indexOf(" the"));
                villagerEntity.setCustomName(new LiteralText(name).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            }
            else{
                String string = " the Zombie";
                zombieVillagerEntity.setCustomName(new LiteralText(Objects.requireNonNull(villagerEntity.getCustomName()).asString() + string).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            }
        }
    }
    public static void generalVillagerUpdate(VillagerEntity entity){
        if (entity.hasCustomName()){
            entity.setCustomName(new LiteralText(Objects.requireNonNull(entity.getCustomName()).asString()).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            if (entity.isBaby() && VillagerNames.CONFIG.villagerGeneralConfig.childNames && !entity.getCustomName().asString().contains(" the Child")){
                entity.setCustomName(new LiteralText(entity.getCustomName().asString() + " the Child").formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            } else if (entity.getVillagerData().getProfession() == VillagerProfession.NITWIT && !entity.getCustomName().asString().contains(VillagerNames.CONFIG.villagerGeneralConfig.nitwitText)){
              String name =  entity.getCustomName().asString().substring(0, entity.getCustomName().asString().indexOf(" the"));
              entity.setCustomName(new LiteralText(name + " the " + VillagerNames.CONFIG.villagerGeneralConfig.nitwitText));
            }
            entity.setCustomNameVisible(true);
        }
    }
    public static void createWanderingTraderNames(WanderingTraderEntity entity){
        if (!entity.hasCustomName() && VillagerNames.CONFIG.villagerGeneralConfig.wanderingTraderNames){
            entity.setCustomName(new LiteralText(generateRandomVillagerName() + " the " + VillagerNames.CONFIG.villagerGeneralConfig.wanderingTraderText).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            entity.setCustomNameVisible(true);
        }
    }
    public static void updateWanderingTraderNames(WanderingTraderEntity entity){
        if (entity.hasCustomName()){
            String fullName = Objects.requireNonNull(entity.getCustomName()).asString();
            String firstName = fullName.substring(0, fullName.indexOf(" the"));
            entity.setCustomName(new LiteralText(firstName + " the " + VillagerNames.CONFIG.villagerGeneralConfig.wanderingTraderText).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            entity.setCustomNameVisible(true);
        }
    }
    public static void updateGolemNames(IronGolemEntity entity){
        if (entity.hasCustomName()){
            entity.setCustomName(new LiteralText(Objects.requireNonNull(entity.getCustomName()).asString()).formatted(VillagerNames.CONFIG.villagerGeneralConfig.villagerTextFormatting.getFormatting()));
            entity.setCustomNameVisible(true);
        }
    }
}
