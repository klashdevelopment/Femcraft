package net.minecraft.client.gui.inventory;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

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
public class GuiBrewingStand extends GuiContainer {
	private static final ResourceLocation brewingStandGuiTextures = new ResourceLocation(
			"textures/gui/container/brewing_stand.png");
	private final InventoryPlayer playerInventory;
	private IInventory tileBrewingStand;

	public GuiBrewingStand(InventoryPlayer playerInv, IInventory parIInventory) {
		super(new ContainerBrewingStand(playerInv, parIInventory));
		this.playerInventory = playerInv;
		this.tileBrewingStand = parIInventory;
	}

	/**+
	 * Draw the foreground layer for the GuiContainer (everything in
	 * front of the items). Args : mouseX, mouseY
	 */
	protected void drawGuiContainerForegroundLayer(int var1, int var2) {
		String s = this.tileBrewingStand.getDisplayName().getUnformattedText();
		this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
				this.ySize - 96 + 2, 4210752);
	}

	/**+
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(brewingStandGuiTextures);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		int k = this.tileBrewingStand.getField(0);
		if (k > 0) {
			int l = (int) (28.0F * (1.0F - (float) k / 400.0F));
			if (l > 0) {
				this.drawTexturedModalRect(i + 97, j + 16, 176, 0, 9, l);
			}

			int i1 = k / 2 % 7;
			switch (i1) {
			case 0:
				l = 29;
				break;
			case 1:
				l = 24;
				break;
			case 2:
				l = 20;
				break;
			case 3:
				l = 16;
				break;
			case 4:
				l = 11;
				break;
			case 5:
				l = 6;
				break;
			case 6:
				l = 0;
			}

			if (l > 0) {
				this.drawTexturedModalRect(i + 65, j + 14 + 29 - l, 185, 29 - l, 12, l);
			}
		}

	}
}