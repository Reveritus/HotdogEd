package com.pushkin.hotdoged.fido;

import android.text.TextUtils;
import android.util.Log;
import com.pushkin.area.Area;
import com.pushkin.area.AreaList;
import com.pushkin.area.AreaListParser;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.Utils;

public class FtnAreaListParser implements AreaListParser {
    private static final String TAG = "FtnAreaListParser";
    private String areasUrl;

    public FtnAreaListParser(String areasUrl) {
        this.areasUrl = areasUrl;
    }

    @Override // com.pushkin.area.AreaListParser
    public void parse(AreaList areaList) throws HotdogedException {
        try {
            String data = Utils.getHttpRequest(this.areasUrl);
            Log.d(TAG, "Server response:\n" + data);
            areaList.clear();
            String[] s = data.split("\n");
            for (String areaData : s) {
                try {
                    Area area = parseAreaData(areaData);
                    areaList.add(area);
                } catch (HotdogedException e) {
                    Log.d(TAG, "Error parsing area: " + areaData);
                }
            }
        } catch (Exception e2) {
            throw new HotdogedException(e2);
        }
    }

    private Area parseAreaData(String areaData) throws HotdogedException {
        if (TextUtils.isEmpty(areaData)) {
            throw new HotdogedException("area data cannot be empty or null");
        }
        Area area = new Area();
        String[] s = areaData.split(",", 4);
        if (s.length >= 1) {
            String name = s[0].trim();
            if (name.length() == 0) {
                throw new HotdogedException("area name cannot be empty");
            }
            area.setName(name);
            if (s.length >= 2) {
                String s1 = s[1].trim();
                if (s1.length() > 0) {
                    try {
                        long timeStamp = Long.parseLong(s1, 10);
                        area.setLastMessageTimestamp(timeStamp);
                        area.formatTimestamp();
                    } catch (NumberFormatException e) {
                        throw new HotdogedException("bad area last message timestamp: " + s1);
                    }
                }
            }
            if (s.length >= 3) {
                String s12 = s[2].trim();
                if (s12.length() > 0) {
                    try {
                        int itemCount = Integer.parseInt(s12, 10);
                        area.setItemCount(itemCount);
                    } catch (NumberFormatException e2) {
                        throw new HotdogedException("bad area item count: " + s12);
                    }
                }
            }
            if (s.length == 4) {
                String desc = s[0].trim();
                if (desc.length() > 0) {
                    area.setDescription(desc);
                }
            }
            return area;
        }
        throw new HotdogedException("bad area data");
    }
}
