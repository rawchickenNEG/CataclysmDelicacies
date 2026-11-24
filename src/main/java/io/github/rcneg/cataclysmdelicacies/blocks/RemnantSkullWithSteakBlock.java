package io.github.rcneg.cataclysmdelicacies.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.utility.TextUtils;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * 远古残骸牛排（两格高、可分层取用的容器方块）。
 *
 * <p>移植到 1.21.1 的关键改动：
 * <ul>
 *   <li>1.20.1 的 {@code use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)}
 *       已被删除，方块交互拆成 {@code useItemOn}（拿到手持物品）和 {@code useWithoutItem}（空手）。
 *       本方块需要按手持容器（碗）判断能否取用，因此重写 {@code useItemOn} 并返回
 *       {@link ItemInteractionResult}。</li>
 *   <li>{@code playerWillDestroy} 现在要返回 {@link BlockState}。</li>
 *   <li>活塞推动行为改由 {@code BlockBehaviour.Properties#pushReaction} 指定，
 *       不再重写 {@code getPistonPushReaction}（见 {@code BlockRegistry}）。</li>
 * </ul>
 */
public class RemnantSkullWithSteakBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 4);
    protected static final VoxelShape CONTAINER_SHAPE_BACK_S = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    protected static final VoxelShape CONTAINER_SHAPE_BACK_W = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    protected static final VoxelShape CONTAINER_SHAPE_BACK_N = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    protected static final VoxelShape CONTAINER_SHAPE_BACK_E = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    protected static final VoxelShape CONTAINER_SHAPE_FRONT_S = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    protected static final VoxelShape CONTAINER_SHAPE_FRONT_W = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    protected static final VoxelShape CONTAINER_SHAPE_FRONT_N = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    protected static final VoxelShape CONTAINER_SHAPE_FRONT_E = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    public final Supplier<Item> servingItem;


    public RemnantSkullWithSteakBlock(Properties props, Supplier<Item> servingItem) {
        super(props);
        this.servingItem = servingItem;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SERVINGS, 0)
                .setValue(PART, Part.FRONT));
    }

    public enum Part implements StringRepresentable {
        FRONT("front"),
        BACK("back");

        private final String name;
        Part(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, SERVINGS, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction extBACKDir = getExtendDir(ctx);

        for (int i = 0; i < 2; i++) {
            BlockPos p = pos.relative(extBACKDir, i);
            if (!level.getWorldBorder().isWithinBounds(p)) return null;
            if (!level.getBlockState(p).canBeReplaced(ctx)) return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, extBACKDir)
                .setValue(PART, Part.FRONT)
                .setValue(SERVINGS, 4);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;
        Direction d = state.getValue(FACING);

        level.setBlock(pos.relative(d, 1), state.setValue(PART, Part.BACK), Block.UPDATE_ALL);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) {
            return super.playerWillDestroy(level, pos, state, player);
        }
        BlockPos front = getStartPos(pos, state);
        Direction facing = state.getValue(FACING);
        BlockState frontState = level.getBlockState(front);

        if (!player.getAbilities().instabuild && frontState.is(this)) {
            Block.dropResources(frontState, level, front, null, player, player.getMainHandItem());
        }

        int flags = Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS;
        for (int i = 0; i < 2; i++) {
            BlockPos p = front.relative(facing, i);
            BlockState s = level.getBlockState(p);
            if (s.is(this)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), flags);
            }
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {

        if (!isStructureValid(level, pos, state)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    private boolean isStructureValid(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos front = getStartPos(pos, state);

        BlockState frontState = level.getBlockState(front);
        if (!frontState.is(this)) return false;

        int servings = frontState.getValue(SERVINGS);

        return isPart(level, front, facing, Part.FRONT, servings)
                && isPart(level, front.relative(facing, 1), facing, Part.BACK, servings);
    }

    private boolean isPart(LevelAccessor level, BlockPos pos, Direction facing, Part part, int servings) {
        BlockState s = level.getBlockState(pos);
        return s.is(this)
                && s.getValue(FACING) == facing
                && s.getValue(PART) == part
                && s.getValue(SERVINGS) == servings;
    }

    private BlockPos getStartPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        return switch (state.getValue(PART)) {
            case FRONT -> pos;
            case BACK -> pos.relative(facing.getOpposite(), 1);
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    private static Direction getExtendDir(BlockPlaceContext ctx) {
        var player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();

        if (player == null) return ctx.getHorizontalDirection();

        double dx = (pos.getX() + 0.5) - player.getX();
        double dz = (pos.getZ() + 0.5) - player.getZ();

        Direction d = Direction.getNearest(dx, 0.0, dz);
        if (d.getAxis().isVertical()) d = ctx.getHorizontalDirection();
        return d;
    }

    public IntegerProperty getServingsProperty() {
        return SERVINGS;
    }

    public ItemStack getServingItem(BlockState state) {
        return new ItemStack((ItemLike)this.servingItem.get());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        return this.takeServing(level, pos, state, player, hand);
    }

    protected ItemInteractionResult takeServing(LevelAccessor level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        BlockPos front = getStartPos(pos, state);
        BlockState frontState = level.getBlockState(front);
        if (!frontState.is(this)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        int servings = frontState.getValue(this.getServingsProperty());

        if (servings == 0) {
            level.playSound(null, front, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);

            Direction facing = frontState.getValue(FACING);

            level.destroyBlock(front, true);
            for (int i = 1; i < 2; i++) {
                BlockPos p = front.relative(facing, i);
                if (level.getBlockState(p).is(this)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        ItemStack serving = this.getServingItem(frontState);
        ItemStack heldStack = player.getItemInHand(hand);

        if (!serving.hasCraftingRemainingItem() || ItemStack.isSameItem(heldStack, serving.getCraftingRemainingItem())) {
            int next = servings - 1;

            setServingsAll(level, front, frontState, next);

            if (!player.getAbilities().instabuild && serving.hasCraftingRemainingItem()) {
                heldStack.shrink(1);
            }

            if (!player.getInventory().add(serving)) {
                player.drop(serving, false);
            }

            level.playSound(null, front, SoundEvents.ARMOR_EQUIP_GENERIC.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }

        player.displayClientMessage(TextUtils.block("feast.use_container", serving.getCraftingRemainingItem().getHoverName()), true);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void setServingsAll(LevelAccessor level, BlockPos anyPos, BlockState anyState, int servings) {
        BlockPos front = getStartPos(anyPos, anyState);
        Direction facing = anyState.getValue(FACING);

        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

        for (int i = 0; i < 2; i++) {
            BlockPos p = front.relative(facing, i);
            BlockState s = level.getBlockState(p);
            if (s.is(this)) {
                level.setBlock(p, s.setValue(SERVINGS, servings), flags);
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CONTAINER_SHAPE_BACK_N;
        if(state.getValue(PART)== Part.BACK){
            switch (state.getValue(FACING)){
                case NORTH -> shape = CONTAINER_SHAPE_BACK_N;
                case SOUTH -> shape = CONTAINER_SHAPE_BACK_S;
                case EAST -> shape = CONTAINER_SHAPE_BACK_E;
                case WEST -> shape = CONTAINER_SHAPE_BACK_W;
            }
        }else if(state.getValue(PART)== Part.FRONT){
            switch (state.getValue(FACING)){
                case NORTH -> shape = CONTAINER_SHAPE_FRONT_N;
                case SOUTH -> shape = CONTAINER_SHAPE_FRONT_S;
                case EAST -> shape = CONTAINER_SHAPE_FRONT_E;
                case WEST -> shape = CONTAINER_SHAPE_FRONT_W;
            }
        }
        return shape;
    }
}
