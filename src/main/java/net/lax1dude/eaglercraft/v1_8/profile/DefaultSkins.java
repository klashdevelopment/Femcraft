/*
 * Copyright (c) 2022-2023 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.profile;

import net.minecraft.util.ResourceLocation;

public enum DefaultSkins {

	DEFAULT_STEVE(0, "Blue Fembunny", new ResourceLocation("eagler:skins/blue_bunny.png"), SkinModel.ALEX),
	DEFAULT_ALEX(1, "Gray Fembunny", new ResourceLocation("eagler:skins/bunny.png"), SkinModel.ALEX),
	TENNIS_STEVE(2, "AI Femboy", new ResourceLocation("eagler:skins/computer.png"), SkinModel.ALEX),
	TENNIS_ALEX(3, "Femboy Maid", new ResourceLocation("eagler:skins/maid.png"), SkinModel.ALEX),
	TUXEDO_STEVE(4, "McDonalds Femboy", new ResourceLocation("eagler:skins/mcdonalds.png"), SkinModel.ALEX),
	TUXEDO_ALEX(5, "Pink Femboy", new ResourceLocation("eagler:skins/pink.png"), SkinModel.ALEX),
	ATHLETE_STEVE(6, "Pink Maid Femboy", new ResourceLocation("eagler:skins/pink_maid.png"), SkinModel.ALEX),
	ATHLETE_ALEX(7, "Purple Femboy", new ResourceLocation("eagler:skins/purple.png"), SkinModel.ALEX),
	CYCLIST_STEVE(8, "Sky Blue Femboy", new ResourceLocation("eagler:skins/sky_blue.png"), SkinModel.ALEX),
	CYCLIST_ALEX(9, "Sunshine Femboy", new ResourceLocation("eagler:skins/sunshine.png"), SkinModel.ALEX);
	
	public static final DefaultSkins[] defaultSkinsMap = new DefaultSkins[10];
	
	public final int id;
	public final String name;
	public final ResourceLocation location;
	public final SkinModel model;
	
	private DefaultSkins(int id, String name, ResourceLocation location, SkinModel model) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.model = model;
	}
	
	public static DefaultSkins getSkinFromId(int id) {
		DefaultSkins e = null;
		if(id >= 0 && id < defaultSkinsMap.length) {
			e = defaultSkinsMap[id];
		}
		if(e != null) {
			return e;
		}else {
			return DEFAULT_STEVE;
		}
	}
	
	static {
		DefaultSkins[] skins = values();
		for(int i = 0; i < skins.length; ++i) {
			defaultSkinsMap[skins[i].id] = skins[i];
		}
	}

}