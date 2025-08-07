package top.zspaces.assistExcavation.client.excavation;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import top.zspaces.assistExcavation.client.Common;
import top.zspaces.assistExcavation.client.config.HotKey.AssistExcavationKeyBindings;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ExcavationHandler {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    // 当前正在挖掘的方块及延迟
    private static BlockPos currentMiningPos = null;
    private static int delayCounter = 0;

    // BFS 邻居方向
    private static final Direction[] DIRECTIONS = {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public static void handleExcavation() {
        // 如果关闭自动挖掘，重置所有状态并退出
        if (!AssistExcavationKeyBindings.isExcavationEnabled()) {
            resetState();
            return;
        }

        // 检查客户端环境
        if (client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager im = client.interactionManager;

        // 读配置
        int delayTicks = Common.getDelayTicks();
        int reach = Common.getReach();
        int mode = Common.getExcavationMode(); // 0=rect, 1=sphere

        // 处理正在挖掘的方块
        if (currentMiningPos != null) {
            // 如果该方块已不在范围内，取消它
            if (!isWithinReach(currentMiningPos, player, reach, mode) || !isWithinServerReach(currentMiningPos, player)) {
                currentMiningPos = null;
            } else {
                // 仍在范围内：执行"挖掘进度更新"
                BlockState state = client.world.getBlockState(currentMiningPos);
                if (state.isAir()) {
                    // 挖完了
                    currentMiningPos = null;
                    delayCounter = delayTicks;
                } else {
                    im.updateBlockBreakingProgress(currentMiningPos, Direction.UP);
                    player.swingHand(Hand.MAIN_HAND);
                }
                return;
            }
        }

        // 延迟控制
        if (delayCounter > 0) {
            delayCounter--;
            return;
        }

        // 发起新一轮 BFS 挖掘
        performBfsMining(player, im, reach, mode);
    }

    private static void performBfsMining(ClientPlayerEntity player,
                                         ClientPlayerInteractionManager im,
                                         int reach, int mode) {
        BlockPos origin = player.getBlockPos();

        // BFS 结构局部化
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>(); // 使用 ArrayDeque 替代 LinkedList

        // 初始：五个方向入队
        for (Direction d : DIRECTIONS) {
            BlockPos nb = origin.offset(d);
            if (isWithinReach(nb, player, reach, mode) && isWithinServerReach(nb, player)) {
                visited.add(nb);
                queue.add(nb);
            }
        }

        // BFS：先近后远
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();

            // 尝试挖掘
            if (tryMine(pos, player, im)) {
                return; // 找到并开始挖一个就退出
            }

            // 扩展到下一级
            for (Direction d : DIRECTIONS) {
                BlockPos next = pos.offset(d);
                if (visited.contains(next)) continue;
                if (!isWithinReach(next, player, reach, mode)) continue;
                if (!isWithinServerReach(next, player)) continue; // 检查服务器可达性
                visited.add(next);
                queue.add(next);
            }
        }
    }

    /**
     * 核心挖掘尝试：符合条件则开始，并设置 delay
     */
    private static boolean tryMine(BlockPos pos,
                                   ClientPlayerEntity player,
                                   ClientPlayerInteractionManager im) {
        // 再次检查服务器可达性
        if (!isWithinServerReach(pos, player)) {
            return false; // 超出服务器允许的范围
        }

        BlockState state = Objects.requireNonNull(client.world).getBlockState(pos);
        if (!state.isAir() && state.getHardness(client.world, pos) >= 0) {
            currentMiningPos = pos;
            im.attackBlock(pos, Direction.UP);
            player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }

    /**
     * 统一的范围判断
     */
    private static boolean isWithinReach(BlockPos target,
                                         ClientPlayerEntity player,
                                         int reach,
                                         int mode) {
        if (mode == 0) {
            return withinRect(target, player, reach);
        } else {
            return withinSphere(target, player, reach);
        }
    }

    /**
     * 检查方块是否在服务器允许的挖掘范围内
     */
    private static boolean isWithinServerReach(BlockPos target, ClientPlayerEntity player) {
        // 获取服务器允许的真实挖掘距离
        double realReach = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        double dx = target.getX() + 0.5 - player.getX();
        double dy = target.getY() + 0.5 - (player.getY() + player.getStandingEyeHeight());
        double dz = target.getZ() + 0.5 - player.getZ();
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq <= realReach * realReach;
    }

    /**
     * 矩形（切比雪夫距离）范围：脚下以上 + 水平 ≤ reach + 垂直 ≤ reach
     */
    private static boolean withinRect(BlockPos target,
                                      ClientPlayerEntity player,
                                      int reach) {
        int footY = player.getBlockPos().getY();
        double eyeY = player.getY() + player.getStandingEyeHeight();
        double px = player.getX(), pz = player.getZ();
        double tx = target.getX() + 0.5, ty = target.getY() + 0.5, tz = target.getZ() + 0.5;

        if (target.getY() < footY) return false;                    // 脚下以上（包括脚部位置）
        if (Math.abs(tx - px) > reach || Math.abs(tz - pz) > reach) return false;  // 水平
        return ty <= eyeY + reach;                                  // 垂直
    }

    /**
     * 球形（欧式距离）范围：distance ≤ reach
     */
    private static boolean withinSphere(BlockPos target,
                                        ClientPlayerEntity player,
                                        int reach) {
        double px = player.getX();
        double py = player.getY() + player.getStandingEyeHeight();
        double pz = player.getZ();
        double tx = target.getX() + 0.5;
        double ty = target.getY() + 0.5;
        double tz = target.getZ() + 0.5;
        double dx = tx - px, dy = ty - py, dz = tz - pz;
        return dx * dx + dy * dy + dz * dz <= (double) reach * reach;
    }

    /**
     * 重置所有状态
     */
    private static void resetState() {
        currentMiningPos = null;
        delayCounter = 0;
    }
}