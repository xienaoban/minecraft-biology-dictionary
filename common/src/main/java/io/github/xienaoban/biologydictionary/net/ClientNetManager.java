package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.net.payload.RequestBiologyDictionaryItemPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestBiologyDictionaryDiscoveryFullPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestBeehiveInfoPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestCommonSkillPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestDiscoveryIncrementalPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestEntityDataPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestEntityOverviewPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestEntityTargetedSkillPacket;
import io.github.xienaoban.biologydictionary.net.payload.RequestServerConfigsPacket;
import io.github.xienaoban.biologydictionary.net.payload.SendStealingDetectedPacket;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class ClientNetManager {
	private ClientNetManager() {}

	public static void requestServerConfigs() {
		ClientNetApi.send(new RequestServerConfigsPacket());
	}

	public static void requestBookItem() {
		ClientNetApi.send(new RequestBiologyDictionaryItemPacket());
	}

	public static void requestBeehiveInfo(BlockPos pos) {
		ClientNetApi.send(new RequestBeehiveInfoPacket(pos));
	}

	public static void requestEntityOverview(EntityType<?> entityType) {
		ClientNetApi.send(new RequestEntityOverviewPacket(EntityUtils.getEntityTypeIdName(entityType)));
	}

	public static void requestEntityData(Entity entity, boolean firstAndFullSync) {
		ClientNetApi.send(new RequestEntityDataPacket(EntityUtils.getId(entity), firstAndFullSync));
	}

	public static void requestBiologyDictionaryDiscoveryFull() {
		ClientNetApi.send(new RequestBiologyDictionaryDiscoveryFullPacket());
	}

	public static void requestDiscoveryIncremental(int entityId, DiscoverySource source) {
		ClientNetApi.send(new RequestDiscoveryIncrementalPacket(entityId, source));
	}

	public static void sendCommonSkill(GeneralSkill skill) {
		ClientNetApi.send(new RequestCommonSkillPacket(skill));
	}

	public static void sendEntityTargetedSkill(Entity entity, EntityTargetedSkill<?> skill) {
		ClientNetApi.send(new RequestEntityTargetedSkillPacket(EntityUtils.getId(entity), skill));
	}

	public static void sendStealingDetected(Entity entity) {
		ClientNetApi.send(new SendStealingDetectedPacket(EntityUtils.getId(entity)));
	}
}
