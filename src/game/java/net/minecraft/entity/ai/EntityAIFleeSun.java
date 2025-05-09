package net.minecraft.entity.ai;

import net.lax1dude.eaglercraft.v1_8.EaglercraftRandom;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
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
public class EntityAIFleeSun extends EntityAIBase {
	private EntityCreature theCreature;
	private double shelterX;
	private double shelterY;
	private double shelterZ;
	private double movementSpeed;
	private World theWorld;

	public EntityAIFleeSun(EntityCreature theCreatureIn, double movementSpeedIn) {
		this.theCreature = theCreatureIn;
		this.movementSpeed = movementSpeedIn;
		this.theWorld = theCreatureIn.worldObj;
		this.setMutexBits(1);
	}

	/**+
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (!this.theWorld.isDaytime()) {
			return false;
		} else if (!this.theCreature.isBurning()) {
			return false;
		} else if (!this.theWorld.canSeeSky(new BlockPos(this.theCreature.posX,
				this.theCreature.getEntityBoundingBox().minY, this.theCreature.posZ))) {
			return false;
		} else {
			Vec3 vec3 = this.findPossibleShelter();
			if (vec3 == null) {
				return false;
			} else {
				this.shelterX = vec3.xCoord;
				this.shelterY = vec3.yCoord;
				this.shelterZ = vec3.zCoord;
				return true;
			}
		}
	}

	/**+
	 * Returns whether an in-progress EntityAIBase should continue
	 * executing
	 */
	public boolean continueExecuting() {
		return !this.theCreature.getNavigator().noPath();
	}

	/**+
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.theCreature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
	}

	private Vec3 findPossibleShelter() {
		EaglercraftRandom random = this.theCreature.getRNG();
		BlockPos blockpos = new BlockPos(this.theCreature.posX, this.theCreature.getEntityBoundingBox().minY,
				this.theCreature.posZ);

		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos1 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
			if (!this.theWorld.canSeeSky(blockpos1) && this.theCreature.getBlockPathWeight(blockpos1) < 0.0F) {
				return new Vec3((double) blockpos1.getX(), (double) blockpos1.getY(), (double) blockpos1.getZ());
			}
		}

		return null;
	}
}