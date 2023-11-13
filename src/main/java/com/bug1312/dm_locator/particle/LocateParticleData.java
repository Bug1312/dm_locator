// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.particle;

import java.util.Locale;

import com.bug1312.dm_locator.Register;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

public class LocateParticleData implements IParticleData {
	public static final Codec<LocateParticleData> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Codec.FLOAT.fieldOf("destination_x").forGetter(LocateParticleData -> LocateParticleData.x),
			Codec.FLOAT.fieldOf("destination_z").forGetter(LocateParticleData -> LocateParticleData.z),
			Codec.INT.fieldOf("arrival_in_ticks").forGetter(LocateParticleData -> LocateParticleData.arrivalInTicks)
		).apply(instance, LocateParticleData::new)
	);
	@SuppressWarnings("deprecation")
	public static final IParticleData.IDeserializer<LocateParticleData> DESERIALIZER = new IParticleData.IDeserializer<LocateParticleData>() {
		@Override
		public LocateParticleData fromCommand(ParticleType<LocateParticleData> particleType, StringReader stringReader) throws CommandSyntaxException {
			stringReader.expect(' ');
			float x = stringReader.readFloat();
			stringReader.expect(' ');
			float z = stringReader.readFloat();
			stringReader.expect(' ');
			int a = stringReader.readInt();
			return new LocateParticleData(x, z, a);
		}

		@Override
		public LocateParticleData fromNetwork(ParticleType<LocateParticleData> particleType, PacketBuffer byteBuff) {
			int x = byteBuff.readVarInt();
			int z = byteBuff.readVarInt();
			int a = byteBuff.readVarInt();

			return new LocateParticleData(x, z, a);
		}
	};

	public final float x;
	public final float z;
	public final int arrivalInTicks;

	public LocateParticleData(float x, float z, int arrivalInTicks) {
		this.x = x;
		this.z = z;
		this.arrivalInTicks = arrivalInTicks;
	}
	
	@Override
	public ParticleType<?> getType() {
		return Register.LOCATE_PARTICLE.get();
	}

	@Override
	public void writeToNetwork(PacketBuffer byteBuff) {
		byteBuff.writeFloat(this.x);
		byteBuff.writeFloat(this.z);
		byteBuff.writeVarInt(this.arrivalInTicks);
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%d %d %d", this.x, this.z, this.arrivalInTicks);
	}

}
