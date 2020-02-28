package tfar.foodtweaks;

import com.google.gson.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfigParser {

	public static final String MODID = "foodtweaks";
	public static Gson g = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final File configFile = new File("config/" + MODID + ".json");

	public static void handleConfig() {

		writeDefaultConfig();
		readConfig();

	}

	private static void writeDefaultConfig() {

		Path path = Paths.get("config/");
		if (!Files.isDirectory(path))
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (configFile.exists()) return;
		JsonArray defaultConfig = new JsonArray();
		for (Item item : Registry.ITEM) {
			if (item.isFood()) {

				JsonObject entry = new JsonObject();
				FoodComponent food = item.getFoodComponent();
				int hunger = food.getHunger();
				float saturation = food.getSaturationModifier();
				boolean alwaysEdible = food.isAlwaysEdible();
				boolean snack = food.isSnack();
				boolean meat = food.isMeat();
				List<Pair<StatusEffectInstance, Float>> effects = food.getStatusEffects();
				entry.addProperty("item", Registry.ITEM.getId(item).toString());
				entry.addProperty("hunger", hunger);
				entry.addProperty("saturation", saturation);
				entry.addProperty("always_edible", alwaysEdible);
				entry.addProperty("snack", snack);
				entry.addProperty("meat", meat);

				if (!effects.isEmpty()) {
					JsonArray effectEntries = new JsonArray();
					for (Pair<StatusEffectInstance, Float> effectEntry : effects) {
						JsonObject effectInstance = new JsonObject();
						StatusEffectInstance statusEffectInstance = effectEntry.getLeft();
						effectInstance.addProperty("id", Registry.STATUS_EFFECT.getId(effectEntry.getLeft().getEffectType()).toString());
						effectInstance.addProperty("duration", statusEffectInstance.getDuration());
						effectInstance.addProperty("amplifier", statusEffectInstance.getAmplifier());
						effectInstance.addProperty("chance", effectEntry.getRight());
						effectEntries.add(effectInstance);
					}
					entry.add("effects", effectEntries);
				}
				defaultConfig.add(entry);
			}
		}

		try {
			FileWriter writer = new FileWriter(configFile);
			writer.write(g.toJson(defaultConfig));
			writer.flush();
		} catch (IOException ugh) {
			//I expect this from a user, but you?!
			throw new RuntimeException("The default config is broken, report to mod author asap!", ugh);
		}
	}

	private static void readConfig() {
		try {
			FileReader reader = new FileReader(configFile);
			JsonArray cfg = new JsonParser().parse(reader).getAsJsonArray();

			for (Item item : Registry.ITEM){
				((ItemInterface)item).setFood(null);
			}

			for (JsonElement entry : cfg) {
				JsonObject entry1 = (JsonObject) entry;
				Item item = Registry.ITEM.get(new Identifier(entry1.get("item").getAsString()));
				int hunger = entry1.get("hunger").getAsInt();
				float saturation = entry1.get("saturation").getAsFloat();
				boolean alwaysEdible = entry1.get("always_edible").getAsBoolean();
				boolean snack = entry1.get("snack").getAsBoolean();
				boolean meat = entry1.get("meat").getAsBoolean();
				FoodComponent.Builder foodComponentBuilder = new FoodComponent.Builder().hunger(hunger).saturationModifier(saturation);
				if (alwaysEdible) foodComponentBuilder.alwaysEdible();
				if (snack) foodComponentBuilder.snack();
				if (meat) foodComponentBuilder.meat();
				JsonElement effects = entry1.get("effects");
				if (effects != null) {
					JsonArray effectArray = effects.getAsJsonArray();
					for (JsonElement effect1 : effectArray) {
						JsonObject object = (JsonObject) effect1;
						StatusEffect statusEffect = Registry.STATUS_EFFECT.get(new Identifier(object.get("id").getAsString()));
						int duration = object.get("duration").getAsInt();
						int amplifier = object.get("amplifier").getAsInt();
						float chance = object.get("chance").getAsFloat();
						StatusEffectInstance statusEffectInstance = new StatusEffectInstance(statusEffect, duration, amplifier);
						foodComponentBuilder.statusEffect(statusEffectInstance, chance);
					}
				}
				FoodComponent foodComponent = foodComponentBuilder.build();
				((ItemInterface) item).setFood(foodComponent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
