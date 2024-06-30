package com.pushkin.hotdoged.export;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.sun.mail.imap.IMAPStore;
import java.io.File;
import java.io.FilenameFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLPrefs {
    private static final String TAG = "XMLPrefs";

    public static void restorePrefs(Context context, String path) {
        String state = Environment.getExternalStorageState();
        if (!"mounted".equals(state) && !"mounted_ro".equals(state)) {
            Log.e(TAG, "Restore failed - external storage not available.");
            Toast.makeText(context, "Restore failed - external storage not available.", 1).show();
            return;
        }
        File extDir = new File(path);
        if (!extDir.isDirectory()) {
            Log.e(TAG, "Restore failed - backup not found.");
            Toast.makeText(context, "Restore failed - backup not found.", 1).show();
            return;
        }
        String[] files = getPrefsFileNames(extDir.getAbsolutePath());
        if (files != null) {
            for (String fileName : files) {
                try {
                    String srcFileName = extDir.getAbsolutePath() + "/" + fileName;
                    String prefsName = fileName.replaceAll("(.+)\\.xml", "$1");
                    loadPrefsFromXML(context, srcFileName, prefsName);
                    Log.d(TAG, "Restored " + srcFileName + " -> " + prefsName);
                } catch (HotdogedException e) {
                    Log.e(TAG, "Failed to restore " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    public static String[] getPrefsFileNames(String prefsDirPath) {
        if (prefsDirPath == null) {
            return new String[0];
        }
        File prefsDir = new File(prefsDirPath);
        if (!prefsDir.isDirectory()) {
            Log.e(TAG, "Path " + prefsDirPath + " is not an accessible directory");
            return new String[0];
        }
        return prefsDir.list(new FilenameFilter() { // from class: com.pushkin.hotdoged.export.XMLPrefs.1
            @Override // java.io.FilenameFilter
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
    }

    public static void loadPrefsFromXML(Context context, String srcFileName, String prefsName) throws HotdogedException {
        Document document = parseXML("file://" + srcFileName);
        if (document == null) {
            throw new HotdogedException("Error parsing XML file: " + srcFileName);
        }
        SharedPreferences.Editor edit = context.getSharedPreferences(prefsName, 0).edit();
        edit.clear();
        edit.commit();
        NodeList nodelist = document.getElementsByTagName("boolean");
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            String varName = null;
            Boolean varValue = false;
            if (node.hasAttributes()) {
                for (int j = 0; j < node.getAttributes().getLength(); j++) {
                    String nodeName = node.getAttributes().item(j).getNodeName();
                    String nodeValue = node.getAttributes().item(j).getNodeValue();
                    if (nodeName != null && nodeValue != null) {
                        if (nodeName.equals(IMAPStore.ID_NAME)) {
                            varName = nodeValue;
                        } else if (nodeName.equals("value")) {
                            varValue = Boolean.valueOf(nodeValue.equalsIgnoreCase("true"));
                        }
                    }
                }
            }
            if (varName != null) {
                Log.d(TAG, "Boolean " + varName + " = " + varValue);
                edit.putBoolean(varName, varValue.booleanValue());
                edit.commit();
            }
        }
        NodeList nodelist2 = document.getElementsByTagName("long");
        for (int i2 = 0; i2 < nodelist2.getLength(); i2++) {
            Node node2 = nodelist2.item(i2);
            String varName2 = null;
            long varValue2 = 0;
            if (node2.hasAttributes()) {
                for (int j2 = 0; j2 < node2.getAttributes().getLength(); j2++) {
                    String nodeName2 = node2.getAttributes().item(j2).getNodeName();
                    String nodeValue2 = node2.getAttributes().item(j2).getNodeValue();
                    if (nodeName2 != null && nodeValue2 != null) {
                        if (nodeName2.equals(IMAPStore.ID_NAME)) {
                            varName2 = nodeValue2;
                        } else if (nodeName2.equals("value")) {
                            varValue2 = Long.valueOf(nodeValue2).longValue();
                        }
                    }
                }
            }
            if (varName2 != null) {
                Log.d(TAG, "Long " + varName2 + " = " + varValue2);
                edit.putLong(varName2, varValue2);
                edit.commit();
            }
        }
        NodeList nodelist3 = document.getElementsByTagName("int");
        for (int i3 = 0; i3 < nodelist3.getLength(); i3++) {
            Node node3 = nodelist3.item(i3);
            String varName3 = null;
            int varValue3 = 0;
            if (node3.hasAttributes()) {
                for (int j3 = 0; j3 < node3.getAttributes().getLength(); j3++) {
                    String nodeName3 = node3.getAttributes().item(j3).getNodeName();
                    String nodeValue3 = node3.getAttributes().item(j3).getNodeValue();
                    if (nodeName3 != null && nodeValue3 != null) {
                        if (nodeName3.equals(IMAPStore.ID_NAME)) {
                            varName3 = nodeValue3;
                        } else if (nodeName3.equals("value")) {
                            varValue3 = Integer.valueOf(nodeValue3).intValue();
                        }
                    }
                }
            }
            if (varName3 != null) {
                Log.d(TAG, "Int " + varName3 + " = " + varValue3);
                edit.putInt(varName3, varValue3);
                edit.commit();
            }
        }
        NodeList nodelist4 = document.getElementsByTagName("string");
        for (int i4 = 0; i4 < nodelist4.getLength(); i4++) {
            Node node4 = nodelist4.item(i4);
            String varName4 = null;
            if (node4.hasAttributes()) {
                int j4 = 0;
                while (true) {
                    if (j4 >= node4.getAttributes().getLength()) {
                        break;
                    }
                    String nodeName4 = node4.getAttributes().item(j4).getNodeName();
                    String nodeValue4 = node4.getAttributes().item(j4).getNodeValue();
                    if (nodeName4 == null || nodeValue4 == null || !nodeName4.equals(IMAPStore.ID_NAME)) {
                        j4++;
                    } else {
                        varName4 = nodeValue4;
                        break;
                    }
                }
            }
            if (varName4 != null && node4.hasChildNodes()) {
                NodeList childNodes = node4.getChildNodes();
                int j5 = 0;
                while (true) {
                    if (j5 < childNodes.getLength()) {
                        if (childNodes.item(j5).getNodeType() != 3) {
                            j5++;
                        } else {
                            String varValue4 = childNodes.item(j5).getNodeValue();
                            Log.d(TAG, "String " + varName4 + " = " + varValue4);
                            edit.putString(varName4, varValue4);
                            edit.commit();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public static Document parseXML(String source) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(source);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
