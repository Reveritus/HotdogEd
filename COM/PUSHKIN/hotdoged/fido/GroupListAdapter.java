package com.pushkin.hotdoged.fido;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.pushkin.area.Area;
import com.pushkin.area.AreaList;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.ServerEntry;
import java.util.Iterator;

public class GroupListAdapter extends BaseAdapter {
    private static final String TAG = "GroupListAdapter";
    private Context context;
    public String error;
    private LayoutInflater lInflater;
    private ServerEntry serverEntry;
    private final AreaList areaList = new AreaList();
    private final AreaList backupAreaList = new AreaList();
    private final int resourceId = R.layout.grouplist_item;

    public void setFilter(String filter) {
        this.areaList.clear();
        if (TextUtils.isEmpty(filter)) {
            this.areaList.addAll(this.backupAreaList);
            notifyDataSetChanged();
        } else if (filter.equals("*")) {
            this.areaList.addAll(this.backupAreaList);
            notifyDataSetChanged();
        } else {
            Iterator<Area> it = this.backupAreaList.iterator();
            while (it.hasNext()) {
                Area item = it.next();
                if (item.getName().toUpperCase().contains(filter.toUpperCase())) {
                    this.areaList.add(item);
                }
            }
            notifyDataSetChanged();
        }
    }

    /* WARN: Code restructure failed: missing block: B:4:0x0028, code lost:
    
        if (r12.moveToFirst() != false) goto L5;
     */
    /* WARN: Code restructure failed: missing block: B:5:0x002a, code lost:
    
        r14 = r12.getLong(r12.getColumnIndex(com.pushkin.hotdoged.export.Constants.INTENT_EXTRA_DBID));
        r18 = r12.getString(r12.getColumnIndex(com.sun.mail.imap.IMAPStore.ID_NAME));
        r9 = r12.getString(r12.getColumnIndex(com.pushkin.hotdoged.export.Constants.INTENT_EXTRA_DESCRIPTION));
        r8 = new com.pushkin.area.Area();
        r8.setDbId(r14);
        r8.setName(r18);
        r8.setDescription(r9);
        r8.setSubscription(1);
        r11 = r20.context.getContentResolver().query(android.net.Uri.withAppendedPath(r21, "groups/" + r14 + "/items"), new java.lang.String[]{"count(_id)", "max(date)"}, null, null, null);
     */
    /* WARN: Code restructure failed: missing block: B:6:0x009b, code lost:
    
        if (r11.moveToFirst() == false) goto L8;
     */
    /* WARN: Code restructure failed: missing block: B:7:0x009d, code lost:
    
        r13 = r11.getInt(0);
        r8.setItemCount(r13);
        r16 = r11.getLong(1);
        r8.setLastMessageTimestamp(r16);
     */
    /* WARN: Code restructure failed: missing block: B:8:0x00af, code lost:
    
        r11.close();
        r20.areaList.add(r8);
        android.util.Log.d(com.pushkin.hotdoged.fido.GroupListAdapter.TAG, "Added area: " + r8);
     */
    /* WARN: Code restructure failed: missing block: B:9:0x00d5, code lost:
    
        if (r12.moveToNext() != false) goto L17;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void fillDBGroups(android.net.Uri r21) {
        /*
            Method dump skipped, instructions count: 276
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.pushkin.hotdoged.fido.GroupListAdapter.fillDBGroups(android.net.Uri):void");
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.areaList.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.areaList.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return -1L;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = this.lInflater.inflate(R.layout.grouplist_item, parent, false);
        }
        fillInView(position, view, parent);
        return view;
    }

    private void fillInView(int position, View view, ViewGroup parent) {
        CheckBox cbGroupName = (CheckBox) view.findViewById(R.id.checkBoxGroupName);
        TextView tvDescription = (TextView) view.findViewById(R.id.textViewDescription);
        TextView tvItemCount = (TextView) view.findViewById(R.id.textViewTotal);
        TextView tvLastMessage = (TextView) view.findViewById(R.id.textViewLastMessage);
        Area area = (Area) getItem(position);
        int itemCount = area.getItemCount();
        if (itemCount > 0) {
            tvItemCount.setText(String.valueOf(itemCount));
            tvItemCount.setVisibility(0);
            cbGroupName.setTypeface(null, 1);
        } else {
            cbGroupName.setTypeface(null, 0);
            tvItemCount.setVisibility(8);
        }
        cbGroupName.setText(area.getName());
        tvDescription.setText(area.getDescription());
        if (area.getLastMessageTimestamp() > 0) {
            try {
                tvLastMessage.setText(area.formatTimestamp());
            } catch (HotdogedException e) {
                tvLastMessage.setText("N/A");
                Log.e(TAG, "Error formatting date for area " + area.getName() + ": " + e.getLocalizedMessage());
            }
        } else {
            tvLastMessage.setText("");
        }
        cbGroupName.setTag(area.getName());
        cbGroupName.setOnCheckedChangeListener(null);
        cbGroupName.setChecked(area.getSubscription() == 1 || area.getSubscription() == 2);
        cbGroupName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.pushkin.hotdoged.fido.GroupListAdapter.1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton button, boolean state) {
                String groupName = (String) button.getTag();
                int position2 = GroupListAdapter.this.areaList.getPositionByName(groupName, false);
                if (position2 >= 0) {
                    Area area2 = GroupListAdapter.this.areaList.get(position2);
                    int ss = GroupListAdapter.this.getNewSubscriptionState(state, area2.getSubscription());
                    Log.d(GroupListAdapter.TAG, "New subscription state for " + groupName + ": " + ss);
                    area2.setSubscription(ss);
                    int position3 = GroupListAdapter.this.backupAreaList.getPositionByName(groupName, false);
                    if (position3 >= 0) {
                        GroupListAdapter.this.backupAreaList.get(position3).setSubscription(ss);
                        return;
                    } else {
                        Log.e(GroupListAdapter.TAG, "Bad backup position: " + position3);
                        return;
                    }
                }
                Log.e(GroupListAdapter.TAG, "Bad position: " + position2);
            }
        });
    }

    protected int getNewSubscriptionState(boolean state, int oldSubscription) {
        if (state) {
            switch (oldSubscription) {
                case 1:
                case 3:
                    return 1;
                case 2:
                default:
                    return 2;
            }
        }
        switch (oldSubscription) {
            case 0:
            case 2:
                return 0;
            case 1:
            default:
                return 3;
        }
    }

    public GroupListAdapter(Context context, ServerEntry serverEntry) throws HotdogedException {
        this.error = null;
        this.context = context;
        this.serverEntry = serverEntry;
        this.lInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        fillDBGroups(serverEntry.getServerUri());
        if (!TextUtils.isEmpty(serverEntry.getAreasurl())) {
            try {
                fillServerGroups(serverEntry.getAreasurl());
            } catch (HotdogedException e) {
                Log.e(TAG, "Error filling server groups: " + e.getLocalizedMessage());
                this.error = e.getLocalizedMessage();
            }
        }
        this.areaList.sort();
        this.backupAreaList.addAll(this.areaList);
    }

    private void fillServerGroups(String areasUrl) throws HotdogedException {
        FtnAreaListParser parser = new FtnAreaListParser(areasUrl);
        AreaList serverAreaList = new AreaList();
        parser.parse(serverAreaList);
        AreaList.mergeAreas(this.areaList, serverAreaList);
    }

    public void setSortOrder(int sortOrder, boolean sortDescending, boolean subscribedFirst) {
        if (this.areaList != null) {
            this.areaList.setSortOrder(sortOrder, sortDescending, subscribedFirst);
        }
        if (this.backupAreaList != null) {
            this.backupAreaList.setSortOrder(sortOrder, sortDescending, subscribedFirst);
        }
    }

    public AreaList getAreasBySubscriptionStatus(int subscription) {
        return this.backupAreaList.getAreasBySubscriptionStatus(subscription);
    }
}
