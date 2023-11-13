// Copyright 2023 Bug1312 (bug@bug1312.com)

package com.bug1312.dm_locator.mixins;

import java.util.UUID;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bug1312.dm_locator.Register;
import com.bug1312.dm_locator.StructureHelper;
import com.bug1312.dm_locator.tiles.FlightPanelTileEntity;
import com.swdteam.common.block.AbstractRotateableWaterLoggableBlock;
import com.swdteam.common.block.IBlockTooltip;
import com.swdteam.common.block.tardis.DataWriterBlock;
import com.swdteam.common.block.tardis.FlightPanelBlock;
import com.swdteam.common.init.DMFlightMode;
import com.swdteam.common.init.DMItems;
import com.swdteam.common.init.DMSoundEvents;
import com.swdteam.common.item.DataModuleItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@Mixin(FlightPanelBlock.class)
public abstract class FlightPanelBlockMixin extends AbstractRotateableWaterLoggableBlock implements IBlockTooltip {
	public FlightPanelBlockMixin(Properties properties) { super(properties); }
	
	private static final BooleanProperty HAS_LOCATOR = BooleanProperty.create("has_locator");
	private static final IntegerProperty CARTRIDGE = DataWriterBlock.CARTRIDGE_TYPE;
	
    private static final VoxelShape N_ADDON_SHAPE = VoxelShapes.box(6/16D, 2/16D, 8/16D, 1, 8/16D, 1);
    private static final VoxelShape E_ADDON_SHAPE = VoxelShapes.box(0, 2/16D, 6/16D, 8/16D, 8/16D, 1);
    private static final VoxelShape S_ADDON_SHAPE = VoxelShapes.box(0, 2/16D, 0, 10/16D, 8/16D, 8/16D);
    private static final VoxelShape W_ADDON_SHAPE = VoxelShapes.box(8/16D, 2/16D, 0, 1, 8/16D, 10/16D);

    private static VoxelShape getAddonShape(BlockState state) {
		switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
			default:
			case NORTH:	return N_ADDON_SHAPE;
			case EAST: 	return E_ADDON_SHAPE;
			case SOUTH:	return S_ADDON_SHAPE;
			case WEST: 	return W_ADDON_SHAPE;
		}
    }
    
	private static boolean isMouseOnLocator(BlockState state, BlockPos pos, Vector3d mouse, IBlockReader world) {
		return state.getValue(HAS_LOCATOR) && getAddonShape(state).bounds().inflate(0.5/16D).contains(mouse.subtract(pos.getX(), pos.getY(), pos.getZ()));
	}
	
	private static void eject(BlockState state, World world, BlockPos pos, FlightPanelTileEntity tile) {
		if (tile.cartridge != null && tile.cartridge.getItem() instanceof DataModuleItem) {
			Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
			
			ItemEntity itemEntity = new ItemEntity(EntityType.ITEM, world);
			itemEntity.absMoveTo((pos.getX() + 0.5 + direction.getStepX()), pos.getY(), (pos.getZ() + 0.5 + direction.getStepZ()), 0, 0);
			itemEntity.setDeltaMovement((direction.getStepX() / 10D), 0, (direction.getStepZ() / 10D));
			itemEntity.setItem(tile.cartridge);
			world.addFreshEntity(itemEntity);
			
			tile.cartridge = ItemStack.EMPTY;
		}
		
		world.setBlockAndUpdate(pos, state.setValue(CARTRIDGE, 0));
	}
	
	@Inject(at = @At("RETURN"), method = "<init>*", remap = false)
	public void constructor(final CallbackInfo ci) {
		this.registerDefaultState(this.defaultBlockState().setValue(HAS_LOCATOR, Boolean.valueOf(false)).setValue(CARTRIDGE, 0));
	}

	@Override
	public void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HAS_LOCATOR, CARTRIDGE);
	}
	
	@Inject(at = @At("RETURN"), method = "getShape", cancellable = true, remap = false)
	public void getShape(final BlockState state, final IBlockReader world, final BlockPos pos, final ISelectionContext context, final CallbackInfoReturnable<VoxelShape> ci) {
		if (state.getValue(HAS_LOCATOR)) ci.setReturnValue(VoxelShapes.or(ci.getReturnValue(), getAddonShape(state)));
	}
	
	@Inject(at = @At("HEAD"), method = "use", cancellable = true, remap = false)
	public void use(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult result, final CallbackInfoReturnable<ActionResultType> ci) {
		if (isMouseOnLocator(state, pos, result.getLocation(), world)) {
			if (!world.isClientSide()) {
				TileEntity tile = world.getBlockEntity(pos);
				if (tile != null && tile instanceof FlightPanelTileEntity) {
					FlightPanelTileEntity panel = (FlightPanelTileEntity) tile;
					ItemStack slotStack = panel.cartridge;
					
					if (slotStack != null && !slotStack.isEmpty()) {
						if (player.isShiftKeyDown()) {
							eject(state, world, pos, panel);
							ci.setReturnValue(ActionResultType.CONSUME);
						}
					} else {
						ItemStack heldStack = player.getItemInHand(handIn);
						if (heldStack != null && !heldStack.isEmpty()) {
							Item item = heldStack.getItem();
							if ((slotStack == null || slotStack.isEmpty()) && item instanceof DataModuleItem) {
								panel.cartridge = heldStack.split(1);
								if (player.abilities.instabuild) heldStack.grow(1);
								
								world.setBlockAndUpdate(pos, state.setValue(CARTRIDGE, heldStack.getItem() == DMItems.DATA_MODULE.get() ? 1 : 2));
								world.playSound((PlayerEntity) null, pos.getX(), pos.getY(), pos.getZ(), DMSoundEvents.TARDIS_MODULE_INSERT.get(), SoundCategory.BLOCKS, 1, 1);
								ci.setReturnValue(ActionResultType.CONSUME);
							}
						}
					}
				}
			}
		} else if (!state.getValue(HAS_LOCATOR)) {
			ItemStack heldStack = player.getItemInHand(handIn);
			if (heldStack != null && !heldStack.isEmpty()) {
				Item item = heldStack.getItem();
				if (item == Register.LOCATOR_ATTACHMENT_ITEM.get()) {
					if (!player.abilities.instabuild) heldStack.shrink(1);

					world.setBlockAndUpdate(pos, state.setValue(HAS_LOCATOR, true));
					world.playSound((PlayerEntity) null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
					ci.setReturnValue(ActionResultType.SUCCESS);
				}
			}
		}
	}

	@Inject(at = @At("TAIL"), method = "use", cancellable = false, remap = false)
	public void addToMap(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult result, final CallbackInfoReturnable<ActionResultType> ci) {
		StructureHelper.isFlyingWithLocator = state.getValue(HAS_LOCATOR);

		if (world.isClientSide() || !state.getValue(HAS_LOCATOR)) return;
		if (isMouseOnLocator(state, pos, result.getLocation(), world)) return;
		
		TileEntity tile = world.getBlockEntity(pos);
		if (tile == null || !(tile instanceof FlightPanelTileEntity)) return;
		
		FlightPanelTileEntity panel = (FlightPanelTileEntity) tile;
		if (panel.cartridge == null || panel.cartridge.isEmpty() || !panel.cartridge.getOrCreateTag().contains("written")) return;
		
		UUID uuid = player.getUUID();
		if (!DMFlightMode.isInFlight(uuid)) return;

		StructureHelper.FLYING_PLAYERS_LOCATOR.put(uuid, panel.cartridge.getOrCreateTag());
	}

	@Override @SuppressWarnings("deprecation")
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState state2, boolean bool) {
		if (!state.is(state2.getBlock())) {
			TileEntity tile = world.getBlockEntity(pos);
			if (tile != null && tile instanceof FlightPanelTileEntity) {
				ItemStack stack = ((FlightPanelTileEntity) tile).cartridge;
				if (stack != null) InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}

		super.onRemove(state, world, pos, state2, bool);
	}
	
	@Override
	public ITextComponent getName(BlockState state, BlockPos pos, Vector3d vector, PlayerEntity player) {
		if (!isMouseOnLocator(state, pos, vector, player.level)) return null;
		
		if (state.getValue(CARTRIDGE) != 0) {
			if (player.isShiftKeyDown()) return Register.TEXT_PANEL_EJECT;
			return null;
		}
		
		return Register.TEXT_PANEL_LOAD;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return state.getValue(HAS_LOCATOR);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return ((Supplier<TileEntity>) FlightPanelTileEntity::new).get();
	}
	
}
