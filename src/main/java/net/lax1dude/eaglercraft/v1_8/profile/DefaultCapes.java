/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
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

public enum DefaultCapes {

	TRANS(0, "Transgender Flag", new ResourceLocation("eagler:capes/trans.png")),
	NO_CAPE(1, "No Cape", null),
	MINECON_2011(2, "Minecon 2011", new ResourceLocation("eagler:capes/01.minecon_2011.png")),
	MINECON_2012(3, "Minecon 2012", new ResourceLocation("eagler:capes/02.minecon_2012.png")),
	MINECON_2013(4, "Minecon 2013", new ResourceLocation("eagler:capes/03.minecon_2013.png")),
	MINECON_2015(5, "Minecon 2015", new ResourceLocation("eagler:capes/04.minecon_2015.png")),
	MINECON_2016(6, "Minecon 2016", new ResourceLocation("eagler:capes/05.minecon_2016.png")),
	MICROSOFT_ACCOUNT(7, "Microsoft Account", new ResourceLocation("eagler:capes/06.microsoft_account.png")),
	MAPMAKER(8, "Realms Mapmaker", new ResourceLocation("eagler:capes/07.mapmaker.png")),
	MOJANG_OLD(9, "Mojang Old", new ResourceLocation("eagler:capes/08.mojang_old.png")),
	MOJANG_NEW(10, "Mojang New", new ResourceLocation("eagler:capes/09.mojang_new.png")),
	JIRA_MOD(11, "Jira Moderator", new ResourceLocation("eagler:capes/10.jira_mod.png")),
	MOJANG_VERY_OLD(12, "Mojang Very Old", new ResourceLocation("eagler:capes/11.mojang_very_old.png")),
	SCROLLS(13, "Scrolls", new ResourceLocation("eagler:capes/12.scrolls.png")),
	COBALT(14, "Cobalt", new ResourceLocation("eagler:capes/13.cobalt.png")),
	TRANSLATOR(15, "Lang Translator", new ResourceLocation("eagler:capes/14.translator.png")),
	MILLIONTH_ACCOUNT(16, "Millionth Player", new ResourceLocation("eagler:capes/15.millionth_account.png")),
	PRISMARINE(17, "Prismarine", new ResourceLocation("eagler:capes/16.prismarine.png")),
	SNOWMAN(18, "Snowman", new ResourceLocation("eagler:capes/17.snowman.png")),
	SPADE(19, "Spade", new ResourceLocation("eagler:capes/18.spade.png")),
	BIRTHDAY(20, "Birthday", new ResourceLocation("eagler:capes/19.birthday.png")),
	DB(21, "dB", new ResourceLocation("eagler:capes/20.db.png")),
	_15TH_ANNIVERSARY(22, "15th Anniversary", new ResourceLocation("eagler:capes/21.15th_anniversary.png")),
	VANILLA(23, "Vanilla", new ResourceLocation("eagler:capes/22.vanilla.png")),
	TIKTOK(24, "TikTok", new ResourceLocation("eagler:capes/23.tiktok.png")),
	PURPLE_HEART(25, "Purple Heart", new ResourceLocation("eagler:capes/24.purple_heart.png")),
	CHERRY_BLOSSOM(26, "Cherry Blossom", new ResourceLocation("eagler:capes/25.cherry_blossom.png"));

	public static final DefaultCapes[] defaultCapesMap = new DefaultCapes[27];
	
	public final int id;
	public final String name;
	public final ResourceLocation location;
	
	private DefaultCapes(int id, String name, ResourceLocation location) {
		this.id = id;
		this.name = name;
		this.location = location;
	}
	
	public static DefaultCapes getCapeFromId(int id) {
		DefaultCapes e = null;
		if(id >= 0 && id < defaultCapesMap.length) {
			e = defaultCapesMap[id];
		}
		if(e != null) {
			return e;
		}else {
			return NO_CAPE;
		}
	}
	
	static {
		DefaultCapes[] capes = values();
		for(int i = 0; i < capes.length; ++i) {
			defaultCapesMap[capes[i].id] = capes[i];
		}
	}

}