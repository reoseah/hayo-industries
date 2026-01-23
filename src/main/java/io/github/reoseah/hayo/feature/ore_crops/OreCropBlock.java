package io.github.reoseah.hayo.feature.ore_crops;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class OreCropBlock extends VegetationBlock implements BonemealableBlock {
    private static final VoxelShape[] SHAPES = Block.boxes(7, i -> Block.column(16, 0, 2 + i * 2));
    public static final int MAX_AGE = 7;

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final BooleanProperty ORE_FERTILIZED = BooleanProperty.create("ore_fertilized");

    public final TagKey<Item> fertilizers;

    public OreCropBlock(TagKey<Item> fertilizers, Properties properties) {
        super(properties);
        this.fertilizers = fertilizers;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(ORE_FERTILIZED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, ORE_FERTILIZED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(this.fertilizers) && !state.getValue(ORE_FERTILIZED) && state.getValue(AGE) != MAX_AGE) {
            if (!level.isClientSide()) {
                if (!player.isCreative()) {
                    stack.setCount(stack.getCount() - 1);
                }

                level.setBlockAndUpdate(pos, state.setValue(ORE_FERTILIZED, true));
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) < 9) {
            return;
        }

        int age = state.getValue(AGE);
        float speed = getGrowthSpeed(this, level, pos);
        if (random.nextInt((int) (25.0F / speed) + 1) == 0) {
            if (age < MAX_AGE - 1) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            } else if (age < MAX_AGE && state.getValue(ORE_FERTILIZED)) {
                level.setBlock(pos, state.setValue(AGE, age + 1).setValue(ORE_FERTILIZED, false), 2);
            }
        }
    }

    protected static float getGrowthSpeed(Block block, BlockGetter level, BlockPos pos) {
        float speed = 1.0F;

        var below = pos.below();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                float contribution = 0.0F;
                var soil = level.getBlockState(below.offset(dx, 0, dy));
                if (soil.is(Blocks.FARMLAND)) {
                    contribution = 1.0F;
                    if (soil.getValue(FarmBlock.MOISTURE) > 0) {
                        contribution = 3.0F;
                    }
                }

                if (dx != 0 || dy != 0) {
                    contribution /= 4.0F;
                }

                speed += contribution;
            }
        }

        return speed;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasSufficientLight(level, pos) && super.canSurvive(state, level, pos);
    }

    protected static boolean hasSufficientLight(LevelReader level, BlockPos pos) {
        return level.getRawBrightness(pos, 0) >= 8;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }
}
