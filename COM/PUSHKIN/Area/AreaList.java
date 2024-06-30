package com.pushkin.area;

import android.text.TextUtils;
import android.util.Log;
import com.pushkin.hotdoged.export.HotdogedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class AreaList extends ArrayList<Area> {
    public static final int DESCRIPTION = 3;
    public static final int ITEM_COUNT = 2;
    public static final int LAST_MESSAGE_TIMESTAMP = 1;
    public static final int NAME = 0;
    private static final long serialVersionUID = 4074816185381889182L;
    private boolean sortDescending;
    private int sortOrder;
    private boolean subscribedFirst;

    /* INFO: Access modifiers changed from: private */
    public final class ComparatorName implements Comparator<Area> {
        private ComparatorName() {
        }

        @Override // java.util.Comparator
        public int compare(Area lhs, Area rhs) {
            int rc = AreaList.this.getSubscribeSortOrderStatus(lhs, rhs);
            if (rc != 0) {
                return rc;
            }
            int rc2 = lhs.getName().compareToIgnoreCase(rhs.getName());
            if (AreaList.this.sortDescending) {
                rc2 *= -1;
            }
            return rc2;
        }
    }

    /* INFO: Access modifiers changed from: private */
    public final class ComparatorDescription implements Comparator<Area> {
        private ComparatorDescription() {
        }

        @Override // java.util.Comparator
        public int compare(Area lhs, Area rhs) {
            int rc = AreaList.this.getSubscribeSortOrderStatus(lhs, rhs);
            if (rc != 0) {
                return rc;
            }
            int rc2 = lhs.getDescription().compareToIgnoreCase(rhs.getDescription());
            if (AreaList.this.sortDescending) {
                rc2 *= -1;
            }
            return rc2;
        }
    }

    /* INFO: Access modifiers changed from: private */
    public final class ComparatorLastMessageTimestamp implements Comparator<Area> {
        private ComparatorLastMessageTimestamp() {
        }

        @Override // java.util.Comparator
        public int compare(Area lhs, Area rhs) {
            long rc = AreaList.this.getSubscribeSortOrderStatus(lhs, rhs);
            if (rc != 0) {
                return (int) rc;
            }
            long rc2 = lhs.getLastMessageTimestamp() - rhs.getLastMessageTimestamp();
            if (AreaList.this.sortDescending) {
                rc2 *= -1;
            }
            return (int) rc2;
        }
    }

    /* INFO: Access modifiers changed from: private */
    public final class ComparatorItemCount implements Comparator<Area> {
        private ComparatorItemCount() {
        }

        @Override // java.util.Comparator
        public int compare(Area lhs, Area rhs) {
            int rc = AreaList.this.getSubscribeSortOrderStatus(lhs, rhs);
            if (rc != 0) {
                return rc;
            }
            int rc2 = lhs.getItemCount() - rhs.getItemCount();
            if (AreaList.this.sortDescending) {
                rc2 *= -1;
            }
            return rc2;
        }
    }

    public boolean isSortDescending() {
        return this.sortDescending;
    }

    public boolean isSubscribedFirst() {
        return this.subscribedFirst;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(int sortOrder, boolean sortDescending, boolean subscribedFirst) {
        if (sortOrder != this.sortOrder || sortDescending != this.sortDescending || this.subscribedFirst != subscribedFirst) {
            this.sortOrder = sortOrder;
            this.sortDescending = sortDescending;
            this.subscribedFirst = subscribedFirst;
            sort();
        }
    }

    public void sort() {
        Collections.sort(this, getSortingComparator(this.sortOrder));
    }

    private Comparator<? super Area> getSortingComparator(int sortOrder) {
        switch (sortOrder) {
            case 1:
                Comparator<? super Area> comparator = new ComparatorLastMessageTimestamp();
                return comparator;
            case 2:
                Comparator<? super Area> comparator2 = new ComparatorItemCount();
                return comparator2;
            case 3:
                Comparator<? super Area> comparator3 = new ComparatorDescription();
                return comparator3;
            default:
                Comparator<? super Area> comparator4 = new ComparatorName();
                return comparator4;
        }
    }

    public void parse(AreaListParser areaListParser) throws HotdogedException {
        areaListParser.parse(this);
    }

    public static void mergeAreas(AreaList areaListDst, AreaList areaListSrc) throws HotdogedException {
        Iterator<Area> it = areaListSrc.iterator();
        while (it.hasNext()) {
            Area serverArea = it.next();
            int pos = getPositionByName(areaListDst, serverArea.getName(), true);
            if (pos >= 0) {
                Log.d("mergeAreas", "Merging " + areaListDst.get(pos) + " from " + serverArea);
                Area.mergeArea(areaListDst.get(pos), serverArea);
            } else {
                areaListDst.add(serverArea);
            }
        }
    }

    public static int getPositionByName(AreaList areaList, String name, boolean strict) {
        if (TextUtils.isEmpty(name)) {
            return -1;
        }
        for (int i = 0; i < areaList.size(); i++) {
            Area area = areaList.get(i);
            if (area != null) {
                String areaName = area.getName();
                if (strict) {
                    if (areaName.equals(name)) {
                        return i;
                    }
                } else if (areaName.trim().equalsIgnoreCase(name.trim())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getPositionByName(String name, boolean strict) {
        return getPositionByName(this, name, strict);
    }

    public AreaList getAreasBySubscriptionStatus(int subscription) {
        AreaList list = new AreaList();
        Iterator<Area> it = iterator();
        while (it.hasNext()) {
            Area area = it.next();
            if (area.getSubscription() == subscription) {
                list.add(area);
            }
        }
        return list;
    }

    /* INFO: Access modifiers changed from: private */
    public int getSubscribeSortOrderStatus(Area lhs, Area rhs) {
        if (this.subscribedFirst) {
            if (lhs.getSubscription() == 1 && rhs.getSubscription() == 0) {
                return -1;
            }
            if (lhs.getSubscription() == 0 && rhs.getSubscription() == 1) {
                return 1;
            }
        }
        return 0;
    }
}
