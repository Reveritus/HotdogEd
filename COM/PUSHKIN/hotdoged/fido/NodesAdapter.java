package com.pushkin.hotdoged.fido;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ch.boye.httpclientandroidlib.HttpHost;
import com.pushkin.ftn.Main;
import com.pushkin.hotdoged.export.HotdogedException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NodesAdapter extends BaseAdapter {
    private static final Uri SERVERS_URI = Uri.parse("content://com.pushkin.hotdoged.provider/ftn/servers");
    private Context context;
    private final LayoutInflater lInflater;
    private final HashMap<String, ArrayList<String>> locations = new HashMap<>();
    private ArrayList<NodeInfo> nodes;

    public NodesAdapter(Context context, ArrayList<NodeInfo> nodes) throws HotdogedException {
        this.context = context;
        this.nodes = nodes;
        Collections.sort(this.nodes);
        this.lInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        fillLocations(this.nodes, getLocations());
    }

    public void setYourLocation(String yourCountry, String yourCity) {
        Iterator<NodeInfo> it = this.nodes.iterator();
        while (it.hasNext()) {
            NodeInfo node = it.next();
            node.setYourCountry(yourCountry);
            node.setYourCity(yourCity);
        }
        Collections.sort(this.nodes);
    }

    public void fillLocations(ArrayList<NodeInfo> nodes, HashMap<String, ArrayList<String>> locations) throws HotdogedException {
        if (nodes == null || locations == null) {
            throw new HotdogedException("Node or location list are null");
        }
        Iterator<NodeInfo> it = nodes.iterator();
        while (it.hasNext()) {
            NodeInfo node = it.next();
            String country = node.getCountry();
            String city = node.getCity().trim();
            if (!TextUtils.isEmpty(country) && locations.containsKey(country)) {
                ArrayList<String> cities = locations.get(country);
                if (!TextUtils.isEmpty(city) && !cities.contains(city)) {
                    cities.add(city);
                }
            } else {
                ArrayList<String> cities2 = new ArrayList<>();
                cities2.add(city);
                locations.put(country, cities2);
            }
        }
        System.out.println("Locations:\n" + locations);
    }

    public NodesAdapter(Context context, String xmlData) throws HotdogedException {
        this.context = context;
        try {
            this.nodes = parseXML(xmlData);
            Collections.sort(this.nodes);
            fillLocations(this.nodes, getLocations());
            this.lInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        } catch (Exception e) {
            this.nodes = new ArrayList<>();
            throw new HotdogedException("Error parsing node config: " + e.getLocalizedMessage());
        }
    }

    public ArrayList<NodeInfo> parseXML(String xmlData) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, HotdogedException {
        if (xmlData == null) {
            throw new HotdogedException("xmlData is null");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlData.getBytes("UTF-8")));
        document.getDocumentElement().normalize();
        NodeList nodelist = document.getElementsByTagName("node");
        ArrayList<NodeInfo> nodes = new ArrayList<>();
        for (int i = 0; i < nodelist.getLength(); i++) {
            String ftnAddress = null;
            String requestBy = null;
            String country = null;
            String city = null;
            String system = null;
            String sysop = null;
            String email = null;
            String protocol = null;
            String ipaddress = null;
            String pntRequestUrl = null;
            String areas = null;
            String areasFull = null;
            String note = null;
            Node node = nodelist.item(i);
            if (node.hasAttributes()) {
                for (int j = 0; j < node.getAttributes().getLength(); j++) {
                    if (node.getAttributes().item(j).getNodeName().equalsIgnoreCase("ftnaddress")) {
                        ftnAddress = node.getAttributes().item(j).getNodeValue();
                    }
                    if (node.getAttributes().item(j).getNodeName().equalsIgnoreCase("requestBy")) {
                        requestBy = node.getAttributes().item(j).getNodeValue();
                    }
                }
            }
            if (ftnAddress == null || requestBy == null) {
                throw new HotdogedException("Each node should have ftnAddress and requestBy attributes");
            }
            if (node.hasChildNodes()) {
                NodeList nodelistNode = node.getChildNodes();
                for (int j2 = 0; j2 < nodelistNode.getLength(); j2++) {
                    Node nodeInfo = nodelistNode.item(j2);
                    if (nodeInfo.getNodeType() == 1 && nodeInfo.hasChildNodes()) {
                        NodeList n = nodeInfo.getChildNodes();
                        for (int k = 0; k < n.getLength(); k++) {
                            Node textNode = n.item(k);
                            if (textNode.getNodeType() == 3) {
                                if (nodeInfo.getNodeName().equalsIgnoreCase("country")) {
                                    country = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("city")) {
                                    city = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("system")) {
                                    system = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("sysop")) {
                                    sysop = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("email")) {
                                    email = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("protocol")) {
                                    protocol = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("ipaddress")) {
                                    ipaddress = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("pntRequestUrl")) {
                                    pntRequestUrl = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("areas")) {
                                    areas = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("areasFull")) {
                                    areasFull = textNode.getNodeValue();
                                }
                                if (nodeInfo.getNodeName().equalsIgnoreCase("note")) {
                                    note = textNode.getNodeValue();
                                }
                            }
                        }
                    }
                }
            }
            if (!alreadyHaveAka(ftnAddress)) {
                NodeInfo n2 = new NodeInfo(ftnAddress, requestBy, country, city, system, sysop, email, protocol, ipaddress, pntRequestUrl, areas, areasFull, null, null, note);
                if (checkNode(n2)) {
                    nodes.add(n2);
                    Main.SystemInfo.getLogger().log("parseXML", "Added node: " + n2.toString());
                } else {
                    Main.SystemInfo.getLogger().log("parseXML", "Bad node config, node skipped: " + n2.toString());
                }
            } else {
                Main.SystemInfo.getLogger().log("parseXML", "Skipped node (already have aka @ it): " + ftnAddress);
            }
        }
        return nodes;
    }

    private boolean checkNode(NodeInfo n) {
        if (n == null || TextUtils.isEmpty(n.getFtnAddress()) || TextUtils.isEmpty(n.getRequestBy()) || TextUtils.isEmpty(n.getSysop()) || TextUtils.isEmpty(n.getCountry()) || TextUtils.isEmpty(n.getCity()) || TextUtils.isEmpty(n.getIpaddress()) || TextUtils.isEmpty(n.getProtocol())) {
            return false;
        }
        return n.getRequestBy().equalsIgnoreCase("email") ? !TextUtils.isEmpty(n.getEmail()) : n.getRequestBy().equalsIgnoreCase(HttpHost.DEFAULT_SCHEME_NAME) && !TextUtils.isEmpty(n.getPntRequestUrl());
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.nodes.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return this.nodes.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return this.nodes.get(position).hashCode();
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = this.lInflater.inflate(R.layout.nodelist_item, parent, false);
        }
        TextView tvNodeInfo = (TextView) view.findViewById(R.id.textViewNodeInfo);
        TextView tvNodeLocation = (TextView) view.findViewById(R.id.textViewLocation);
        TextView tvNodeInfoExt = (TextView) view.findViewById(R.id.textViewNodeInfoExt);
        ImageView ivRequestBy = (ImageView) view.findViewById(R.id.imageViewRequestBy);
        ImageView ivAreas = (ImageView) view.findViewById(R.id.imageViewAreas);
        if (this.nodes != null && this.nodes.size() > position) {
            NodeInfo node = this.nodes.get(position);
            tvNodeInfo.setText(node.getFtnAddress() + ", " + node.getSystem());
            tvNodeLocation.setText(node.getCountry() + ", " + node.getCity());
            tvNodeInfoExt.setText(node.getSysop());
            if (node.getRequestBy().equalsIgnoreCase(HttpHost.DEFAULT_SCHEME_NAME)) {
                ivRequestBy.setImageResource(17301516);
            } else if (node.getRequestBy().equalsIgnoreCase("email")) {
                ivRequestBy.setImageResource(17301545);
            } else {
                ivRequestBy.setImageResource(17301560);
            }
            if (node.getAreas() != null && node.getAreasFull() == null) {
                ivAreas.setImageResource(17301609);
            } else if (node.getAreas() == null && node.getAreasFull() != null) {
                ivAreas.setImageResource(17301611);
            } else if (node.getAreas() != null && node.getAreasFull() != null) {
                ivAreas.setImageResource(17301611);
            } else {
                ivAreas.setImageResource(17301610);
            }
        }
        return view;
    }

    private boolean alreadyHaveAka(String bossNode) {
        Cursor cursor = null;
        try {
            cursor = this.context.getContentResolver().query(SERVERS_URI, null, "server_name = ?", new String[]{bossNode}, null);
            if (cursor.moveToFirst()) {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                return true;
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return false;
        } catch (Throwable th) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            throw th;
        }
    }

    public HashMap<String, ArrayList<String>> getLocations() {
        return this.locations;
    }
}
