/*
 * Copyright (C) 2025  Carpet-LMS-Addition contributors
 * https://github.com/Citrus-Union/Carpet-LMS-Addition

 * Carpet LMS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Carpet LMS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Carpet LMS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.nm.lms.carpetlmsaddition.storage.getitem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class GetItemSlotSelector {
    private GetItemSlotSelector() {}

    public static List<Target> select(List<Target> targets, int targetCount) {
        if (targetCount <= 0 || targets.isEmpty()) {
            return List.of();
        }

        List<TargetGroup> groups = toGroups(targets);

        int already = 0;
        List<Target> selected = new ArrayList<>();
        while (already < targetCount && !groups.isEmpty()) {
            int index = lowerBound(groups, targetCount - already);
            TargetGroup group = groups.get(index);
            Target target = group.removeLast();
            if (group.isEmpty()) {
                groups.remove(index);
            }
            selected.add(target);
            already += target.count();
        }
        return selected;
    }

    private static List<TargetGroup> toGroups(List<Target> targets) {
        List<Target> sorted = new ArrayList<>(targets);
        sorted.sort(Comparator.comparingInt(Target::count));

        List<TargetGroup> groups = new ArrayList<>();
        TargetGroup current = null;
        for (Target target : sorted) {
            if (current == null || current.count() != target.count()) {
                current = new TargetGroup(target.count());
                groups.add(current);
            }
            current.add(target);
        }
        groups.forEach(TargetGroup::sort);
        return groups;
    }

    private static int lowerBound(List<TargetGroup> groups, int count) {
        if (count >= groups.getLast().count()) {
            return groups.size() - 1;
        }
        int low = 0;
        int high = groups.size();
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (groups.get(mid).count() < count) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    private static final class TargetGroup {
        private final int count;
        private final List<Target> targets = new ArrayList<>();

        TargetGroup(int count) {
            this.count = count;
        }

        int count() {
            return this.count;
        }

        void add(Target target) {
            this.targets.add(target);
        }

        void sort() {
            this.targets
                .sort(Comparator.comparing(Target::noShulkerBox).thenComparingInt(target -> target.pos().getY()));
        }

        Target removeLast() {
            return this.targets.removeLast();
        }

        boolean isEmpty() {
            return this.targets.isEmpty();
        }
    }

    public record Target(ResourceKey<Level> dimension, BlockPos pos, int slotIndex, int count, boolean noShulkerBox) {
    }
}
