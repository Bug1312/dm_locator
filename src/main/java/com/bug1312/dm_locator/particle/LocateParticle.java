// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.particle;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LocateParticle extends SpriteTexturedParticle {

	private final float toX;
	private final float toZ;
	private final float yRot;

	protected LocateParticle(ClientWorld world, double x, double y, double z, double speedX, double speedY, double speedZ, LocateParticleData data) {
		super(world, x, y, z, speedX, speedY, speedZ);
		this.quadSize = 3F;
		this.lifetime = data.arrivalInTicks;
		
		this.toX = data.x;
		this.toZ = data.z;
		
		this.yRot = (float) MathHelper.atan2(this.x - this.toX, this.z - this.toZ);
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	@Override
	public int getLightColor(float f) {
		int i = super.getLightColor(f);
		int k = i >> 16 & 255;
		return 240 | k << 16;
	}

	@Override
	public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float delta) {
		float h = this.yRot;
		float i = 1.57F; // 90 degrees
		this.renderSwoosh(vertexBuilder, renderInfo, delta, quaternion -> {
			quaternion.mul(Vector3f.YP.rotation(h));
			quaternion.mul(Vector3f.XP.rotation(-i));
		});
		this.renderSwoosh(vertexBuilder, renderInfo, delta, quaternion -> {
			quaternion.mul(Vector3f.YP.rotation((float) -Math.PI + h));
			quaternion.mul(Vector3f.XP.rotation(i));
		});
	}
	
	private void renderSwoosh(IVertexBuilder vertexBuilder, ActiveRenderInfo renderInfo, float delta, Consumer<Quaternion> consumer) {
		Vector3d vec3 = renderInfo.getPosition();
		float g = (float) (MathHelper.lerp(delta, this.xo, this.x) - vec3.x());
		float h = (float) (this.y - vec3.y());
		float i = (float) (MathHelper.lerp(delta, this.zo, this.z) - vec3.z());

		Vector3f vector3f = new Vector3f(0.5F, 0.5F, 0.5F);
		vector3f.normalize();
		Quaternion quaternion = new Quaternion(vector3f, 0.0F, true);
		consumer.accept(quaternion);
		Vector3f vector3f2 = new Vector3f(-1.0F, -1.0F, 0.0F);
		vector3f2.transform(quaternion);
		Vector3f[] vector3fs = new Vector3f[] { new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F) };
		float j = this.getQuadSize(delta);

		for (int k = 0; k < 4; ++k) {
			Vector3f vector3f3 = vector3fs[k];
			vector3f3.transform(quaternion);
			vector3f3.mul(j);
			vector3f3.add(g, h, i);
		}

		float l = this.getU0();
		float m = this.getU1();
		float n = this.getV0();
		float o = this.getV1();
		int p = this.getLightColor(delta);
				
//		float a = (float) (MathHelper.lerp(delta + (this.lifetime - this.age), 0, 1));
		float a = (float) ((this.lifetime - this.age) / (float) this.lifetime);
		vertexBuilder.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(m, o).color(this.rCol, this.gCol, this.bCol, a).uv2(p).endVertex();
		vertexBuilder.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(m, n).color(this.rCol, this.gCol, this.bCol, a).uv2(p).endVertex();
		vertexBuilder.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(l, n).color(this.rCol, this.gCol, this.bCol, a).uv2(p).endVertex();
		vertexBuilder.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(l, o).color(this.rCol, this.gCol, this.bCol, a).uv2(p).endVertex();
	}
	
	@Override
	public void tick() {
		this.xo = this.x;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			int i = this.lifetime - this.age;
			double d = 1.0 / (double) i;
			this.x = MathHelper.lerp(d, this.x, toX);
			this.z = MathHelper.lerp(d, this.z, toZ);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Factory implements IParticleFactory<LocateParticleData> {
		private final IAnimatedSprite sprite;

		public Factory(IAnimatedSprite sprite) {
			this.sprite = sprite;
		}

		public Particle createParticle(LocateParticleData data, ClientWorld world, double x, double y, double z, double speedX, double speedY, double speedZ) {
			LocateParticle particle = new LocateParticle(world, x, y, z, speedX, speedY, speedZ, data);
			particle.pickSprite(this.sprite);
			particle.setAlpha(1.0F);
			return particle;
		}
	}
	
}
