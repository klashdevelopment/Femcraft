package net.minecraft.village;

import java.util.Iterator;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * 
 * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * 
 * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
public class VillageSiege {
	private World worldObj;
	private boolean field_75535_b;
	private int field_75536_c = -1;
	private int field_75533_d;
	private int field_75534_e;
	private Village theVillage;
	private int field_75532_g;
	private int field_75538_h;
	private int field_75539_i;

	public VillageSiege(World worldIn) {
		this.worldObj = worldIn;
	}

	/**+
	 * Runs a single tick for the village siege
	 */
	public void tick() {
		if (this.worldObj.isDaytime()) {
			this.field_75536_c = 0;
		} else if (this.field_75536_c != 2) {
			if (this.field_75536_c == 0) {
				float f = this.worldObj.getCelestialAngle(0.0F);
				if ((double) f < 0.5D || (double) f > 0.501D) {
					return;
				}

				this.field_75536_c = this.worldObj.rand.nextInt(10) == 0 ? 1 : 2;
				this.field_75535_b = false;
				if (this.field_75536_c == 2) {
					return;
				}
			}

			if (this.field_75536_c != -1) {
				if (!this.field_75535_b) {
					if (!this.func_75529_b()) {
						return;
					}

					this.field_75535_b = true;
				}

				if (this.field_75534_e > 0) {
					--this.field_75534_e;
				} else {
					this.field_75534_e = 2;
					if (this.field_75533_d > 0) {
						this.spawnZombie();
						--this.field_75533_d;
					} else {
						this.field_75536_c = 2;
					}

				}
			}
		}
	}

	private boolean func_75529_b() {
		List list = this.worldObj.playerEntities;
		Iterator iterator = list.iterator();

		while (true) {
			if (!iterator.hasNext()) {
				return false;
			}

			EntityPlayer entityplayer = (EntityPlayer) iterator.next();
			if (!entityplayer.isSpectator()) {
				this.theVillage = this.worldObj.getVillageCollection().getNearestVillage(new BlockPos(entityplayer), 1);
				if (this.theVillage != null && this.theVillage.getNumVillageDoors() >= 10
						&& this.theVillage.getTicksSinceLastDoorAdding() >= 20
						&& this.theVillage.getNumVillagers() >= 20) {
					BlockPos blockpos = this.theVillage.getCenter();
					float f = (float) this.theVillage.getVillageRadius();
					boolean flag = false;

					for (int i = 0; i < 10; ++i) {
						float f1 = this.worldObj.rand.nextFloat() * 3.1415927F * 2.0F;
						this.field_75532_g = blockpos.getX() + (int) ((double) (MathHelper.cos(f1) * f) * 0.9D);
						this.field_75538_h = blockpos.getY();
						this.field_75539_i = blockpos.getZ() + (int) ((double) (MathHelper.sin(f1) * f) * 0.9D);
						flag = false;

						List<Village> lst = this.worldObj.getVillageCollection().getVillageList();
						for (int j = 0, l = lst.size(); j < l; ++j) {
							Village village = lst.get(i);
							if (village != this.theVillage && village.func_179866_a(
									new BlockPos(this.field_75532_g, this.field_75538_h, this.field_75539_i))) {
								flag = true;
								break;
							}
						}

						if (!flag) {
							break;
						}
					}

					if (flag) {
						return false;
					}

					Vec3 vec3 = this
							.func_179867_a(new BlockPos(this.field_75532_g, this.field_75538_h, this.field_75539_i));
					if (vec3 != null) {
						break;
					}
				}
			}
		}

		this.field_75534_e = 0;
		this.field_75533_d = 20;
		return true;
	}

	private boolean spawnZombie() {
		Vec3 vec3 = this.func_179867_a(new BlockPos(this.field_75532_g, this.field_75538_h, this.field_75539_i));
		if (vec3 == null) {
			return false;
		} else {
			EntityZombie entityzombie;
			try {
				entityzombie = new EntityZombie(this.worldObj);
				entityzombie.onInitialSpawn(this.worldObj.getDifficultyForLocation(new BlockPos(entityzombie)),
						(IEntityLivingData) null);
				entityzombie.setVillager(false);
			} catch (Exception exception) {
				EagRuntime.debugPrintStackTrace(exception);
				return false;
			}

			entityzombie.setLocationAndAngles(vec3.xCoord, vec3.yCoord, vec3.zCoord,
					this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
			this.worldObj.spawnEntityInWorld(entityzombie);
			BlockPos blockpos = this.theVillage.getCenter();
			entityzombie.setHomePosAndDistance(blockpos, this.theVillage.getVillageRadius());
			return true;
		}
	}

	private Vec3 func_179867_a(BlockPos parBlockPos) {
		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos = parBlockPos.add(this.worldObj.rand.nextInt(16) - 8, this.worldObj.rand.nextInt(6) - 3,
					this.worldObj.rand.nextInt(16) - 8);
			if (this.theVillage.func_179866_a(blockpos) && SpawnerAnimals.canCreatureTypeSpawnAtLocation(
					EntityLiving.SpawnPlacementType.ON_GROUND, this.worldObj, blockpos)) {
				return new Vec3((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ());
			}
		}

		return null;
	}
}