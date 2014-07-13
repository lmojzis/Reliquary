package xreliquary.client;

import lib.enderwizards.sandstone.init.ContentHandler;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.MinecraftForgeClient;
import xreliquary.blocks.tile.TileEntityMortar;
import xreliquary.client.render.ItemRendererApothecaryMortar;
import xreliquary.client.render.ItemRendererHandgun;
import xreliquary.client.render.RenderApothecaryCauldron;
import xreliquary.client.render.RenderApothecaryMortar;
import xreliquary.client.render.RenderShot;
import xreliquary.client.render.RenderThrown;
import xreliquary.common.CommonProxy;
import xreliquary.entities.EntityBlazeShot;
import xreliquary.entities.EntityBusterShot;
import xreliquary.entities.EntityConcussiveShot;
import xreliquary.entities.EntityCondensedFertility;
import xreliquary.entities.EntityCondensedSplashAphrodite;
import xreliquary.entities.EntityCondensedSplashBlindness;
import xreliquary.entities.EntityCondensedSplashConfusion;
import xreliquary.entities.EntityCondensedSplashHarm;
import xreliquary.entities.EntityCondensedSplashPoison;
import xreliquary.entities.EntityCondensedSplashRuin;
import xreliquary.entities.EntityCondensedSplashSlowness;
import xreliquary.entities.EntityCondensedSplashWeakness;
import xreliquary.entities.EntityCondensedSplashWither;
import xreliquary.entities.EntityEnderShot;
import xreliquary.entities.EntityEnderStaffProjectile;
import xreliquary.entities.EntityExorcismShot;
import xreliquary.entities.EntityGlowingWater;
import xreliquary.entities.EntityHolyHandGrenade;
import xreliquary.entities.EntityKrakenSlime;
import xreliquary.entities.EntityNeutralShot;
import xreliquary.entities.EntitySandShot;
import xreliquary.entities.EntitySeekerShot;
import xreliquary.entities.EntitySpecialSnowball;
import xreliquary.entities.EntityStormShot;
import xreliquary.event.ClientEventHandler;
import xreliquary.lib.Names;
import xreliquary.lib.Reference;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		super.preInit();
	}

	@Override
	public void init() {
		super.init();
		FMLCommonHandler.instance().bus().register(new ClientEventHandler());
		// FMLCommonHandler.instance().bus().register(new HandgunHUD());

		this.registerRenderers();
	}

	public void registerRenderers() {

		RenderingRegistry.registerEntityRenderingHandler(EntityBlazeShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityBusterShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityConcussiveShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityExorcismShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityNeutralShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntitySeekerShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntitySandShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityStormShot.class, new RenderShot());
		RenderingRegistry.registerEntityRenderingHandler(EntityHolyHandGrenade.class, new RenderThrown(12));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpecialSnowball.class, new RenderSnowball(Items.snowball));
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderStaffProjectile.class, new RenderSnowball(Items.ender_pearl));
		RenderingRegistry.registerEntityRenderingHandler(EntityKrakenSlime.class, new RenderThrown(13));
		RenderingRegistry.registerEntityRenderingHandler(EntityGlowingWater.class, new RenderThrown(Reference.WATER_SPRITE));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashAphrodite.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.APHRODITE_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashPoison.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.POISON_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashHarm.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.ACID_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashConfusion.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.CONFUSION_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashSlowness.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.SLOWING_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashWeakness.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.WEAKNESS_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashWither.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.WITHER_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashBlindness.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.BLINDING_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedSplashRuin.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.RUINATION_META));
		RenderingRegistry.registerEntityRenderingHandler(EntityCondensedFertility.class, new RenderThrown(Reference.SPLASH_POTION_SPRITE + Reference.FERTILIZER_META));

		RenderingRegistry.registerBlockHandler(new RenderApothecaryCauldron());

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMortar.class, new RenderApothecaryMortar());

		MinecraftForgeClient.registerItemRenderer(ItemBlock.getItemFromBlock(ContentHandler.getBlock(Names.apothecary_mortar)), new ItemRendererApothecaryMortar());
		MinecraftForgeClient.registerItemRenderer(ContentHandler.getItem(Names.handgun), new ItemRendererHandgun());
	}

}
