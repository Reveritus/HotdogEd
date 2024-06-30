package jnode.ftn.tosser;

import com.pushkin.ftn.Echoarea;
import com.pushkin.ftn.Echomail;
import com.pushkin.ftn.EchomailAwaiting;
import com.pushkin.ftn.Link;
import com.pushkin.ftn.LinkOption;
import com.pushkin.ftn.Main;
import com.pushkin.ftn.Netmail;
import com.pushkin.hotdoged.export.HotdogedException;
import com.pushkin.hotdoged.export.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import jnode.event.FileTossingEvent;
import jnode.event.FtnMessageReceivedEvent;
import jnode.event.Notifier;
import jnode.event.UnknownFileEvent;
import jnode.ftn.FtnTools;
import jnode.ftn.types.Ftn2D;
import jnode.ftn.types.FtnAddress;
import jnode.ftn.types.FtnMessage;
import jnode.ftn.types.FtnPkt;
import jnode.logger.Logger;
import jnode.protocol.io.Message;

public class FtnTosser {
    private static final String EXT_DIR_SUFFIX = "ftn/incoming";
    private static final String TAG = "FtnTosser";
    private static final Logger logger = Logger.getLogger(FtnTosser.class, Main.SystemInfo.getEventsArray());
    private Map<String, Integer> tossed = new HashMap();
    private Map<String, Integer> bad = new HashMap();
    private Set<Link> pollLinks = new HashSet();

    private void tossNetmail(FtnMessage netmail, boolean secure) {
        Link routeVia;
        boolean drop = FtnTools.isNetmailMustBeDropped(netmail, secure);
        if (drop) {
            Integer n = this.bad.get("netmail");
            this.bad.put("netmail", Integer.valueOf(n == null ? 1 : n.intValue() + 1));
            return;
        }
        if ((netmail.getAttribute() & 16384) > 0) {
            FtnTools.writeReply(netmail, "ARQ reply", "Your message was successfully reached this system");
        }
        Link routeVia2 = FtnTools.getRouting(netmail);
        Netmail dbnm = new Netmail();
        dbnm.setRouteVia(routeVia2);
        dbnm.setDate(netmail.getDate());
        dbnm.setFromFTN(netmail.getFromAddr().toString());
        dbnm.setToFTN(netmail.getToAddr().toString());
        dbnm.setFromName(netmail.getFromName());
        dbnm.setToName(netmail.getToName());
        dbnm.setSubject(netmail.getSubject());
        dbnm.setText(netmail.getFromAddr().fmpt() + netmail.getToAddr().topt() + netmail.getText());
        dbnm.setAttr(netmail.getAttribute());
        try {
            dbnm.save();
        } catch (HotdogedException e) {
            Main.SystemInfo.getLogger().log(TAG, "Netmail " + dbnm.toString() + " could not be saved: " + e.getMessage());
        }
        Integer n2 = this.tossed.get("netmail");
        this.tossed.put("netmail", Integer.valueOf(n2 == null ? 1 : n2.intValue() + 1));
        Link bossNode = null;
        try {
            Link bossNode2 = new Link(Main.info.getBossAddress());
            bossNode = bossNode2;
        } catch (HotdogedException e1) {
            e1.printStackTrace();
        }
        if (routeVia2 == null) {
            logger.l4(String.format("Netmail %s -> %s is not transferred ( routing not found )", netmail.getFromAddr().toString(), netmail.getToAddr().toString()));
            Notifier.INSTANSE.notify(new FtnMessageReceivedEvent(netmail));
        } else if (routeVia2.equals(bossNode)) {
            logger.l4(String.format("Netmail %s -> %s is not transferred ( reached it's destination )", netmail.getFromAddr().toString(), netmail.getToAddr().toString()));
            Notifier.INSTANSE.notify(new FtnMessageReceivedEvent(netmail));
        } else {
            try {
                routeVia = new Link(routeVia2.getId());
            } catch (HotdogedException e2) {
                e = e2;
            }
            try {
                logger.l4(String.format("Netmail %s -> %s transferred via %s", netmail.getFromAddr().toString(), netmail.getToAddr().toString(), routeVia.getLinkAddress()));
                if (FtnTools.getOptionBooleanDefTrue(routeVia, LinkOption.BOOLEAN_CRASH_NETMAIL)) {
                    System.out.println("Poll to " + routeVia + " needs to be created");
                }
            } catch (HotdogedException e3) {
                e = e3;
                e.printStackTrace();
            }
        }
    }

    private void tossEchomail(FtnMessage echomail, Link link, boolean secure) {
        if (!secure) {
            logger.l3("Echomail via unsecure is dropped");
            return;
        }
        Echoarea area = FtnTools.getAreaByName(echomail.getArea(), link);
        if (area == null) {
            logger.l3("Echoarea " + echomail.getArea() + " is not avalible for " + link.getLinkAddress());
            Integer n = this.bad.get(echomail.getArea());
            this.bad.put(echomail.getArea(), Integer.valueOf(n != null ? n.intValue() + 1 : 1));
        } else if (FtnTools.isADupe(area, echomail.getMsgid())) {
            logger.l3("Message " + echomail.getArea() + " " + echomail.getMsgid() + " is a dupe");
            Integer n2 = this.bad.get(echomail.getArea());
            this.bad.put(echomail.getArea(), Integer.valueOf(n2 != null ? n2.intValue() + 1 : 1));
        } else {
            Echomail mail = new Echomail();
            mail.setArea(area);
            mail.setDate(echomail.getDate());
            mail.setFromFTN(echomail.getFromAddr().toString());
            mail.setFromName(echomail.getFromName());
            mail.setToName(echomail.getToName());
            mail.setSubject(echomail.getSubject());
            mail.setText(echomail.getText());
            mail.setSeenBy(FtnTools.write2D(echomail.getSeenby(), true));
            mail.setPath(FtnTools.write2D(echomail.getPath(), false));
            try {
                mail.save();
                Notifier.INSTANSE.notify(new FtnMessageReceivedEvent(echomail));
            } catch (HotdogedException e) {
                Main.SystemInfo.getLogger().log(TAG, "Error saving echomail: " + e.getMessage());
            }
            Integer n3 = this.tossed.get(echomail.getArea());
            this.tossed.put(echomail.getArea(), Integer.valueOf(n3 != null ? n3.intValue() + 1 : 1));
        }
    }

    public int tossIncoming(Message message, Link link) {
        if (message == null) {
            return 0;
        }
        try {
            FtnTools.unpack(message);
            for (File file : tossInboundDirectory()) {
                logger.l1("Moving file " + file.getName() + " to bad dir.");
                Utils.copyFileToExtMemory(file.getAbsolutePath(), "ftn/bad", true);
            }
            return 0;
        } catch (HotdogedException | IOException e) {
            logger.l1("Exception file tossing message " + message.getMessageName(), e);
            return 1;
        }
    }

    public List<File> tossInboundDirectory() {
        List<File> badFileList = new ArrayList<>();
        synchronized (FtnTosser.class) {
            Set<Link> poll = new HashSet<>();
            File inbound = new File(Main.getInbound());
            File[] listFiles = inbound.listFiles();
            int length = listFiles.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                File file = listFiles[i];
                if (Main.info.needsStop) {
                    Main.SystemInfo.getLogger().l5("Tossing stopped by user request");
                    break;
                }
                String loname = file.getName().toLowerCase();
                Notifier.INSTANSE.notify(new FileTossingEvent(loname));
                if (loname.matches(FtnTools.PKT_REGEX)) {
                    try {
                        Message m = new Message(file);
                        logger.l4("Распаковка файла " + file.getAbsolutePath());
                        FtnPkt pkt = new FtnPkt();
                        pkt.unpack(m.getInputStream());
                        while (true) {
                            FtnMessage ftnm = pkt.getNextMessage();
                            if (ftnm == null) {
                                break;
                            } else if (Main.info.needsStop) {
                                Main.SystemInfo.getLogger().l5("Tossing stopped by user request");
                                break;
                            } else if (ftnm.isNetmail()) {
                                tossNetmail(ftnm, true);
                            } else {
                                tossEchomail(ftnm, null, true);
                            }
                        }
                        file.delete();
                    } catch (Exception e) {
                        logger.l3("Неудачная распаковка " + file.getAbsolutePath(), e);
                        badFileList.add(file);
                    }
                    i++;
                } else if (loname.matches("(s|u)inb\\d*.pkt")) {
                    try {
                        Message m2 = new Message(file);
                        logger.l4("Распаковка файла " + file.getAbsolutePath());
                        FtnPkt pkt2 = new FtnPkt();
                        pkt2.unpack(m2.getInputStream());
                        Link link = new Link(pkt2.getFromAddr());
                        boolean secure = loname.charAt(0) == 's';
                        if (secure && !FtnTools.getOptionBooleanDefFalse(link, LinkOption.BOOLEAN_IGNORE_PKTPWD) && !link.getPaketPassword().equalsIgnoreCase(pkt2.getPassword())) {
                            logger.l2("Pkt password mismatch - package moved to inbound");
                            badFileList.add(file);
                        } else {
                            while (true) {
                                FtnMessage ftnm2 = pkt2.getNextMessage();
                                if (ftnm2 == null) {
                                    break;
                                } else if (Main.info.needsStop) {
                                    Main.SystemInfo.getLogger().l5("Tossing stopped by user request");
                                    break;
                                } else if (ftnm2.isNetmail()) {
                                    tossNetmail(ftnm2, secure);
                                } else {
                                    tossEchomail(ftnm2, link, secure);
                                }
                            }
                            file.delete();
                        }
                    } catch (Exception e2) {
                        logger.l3("Неудачная распаковка " + file.getAbsolutePath(), e2);
                        badFileList.add(file);
                    }
                    i++;
                } else if (loname.matches(FtnTools.LO_REGEX)) {
                    FtnAddress address = Main.info.getAddress().clone();
                    address.setPoint(0);
                    try {
                        address.setNet(Integer.parseInt(loname.substring(0, 4), 16));
                        address.setNode(Integer.parseInt(loname.substring(4, 8), 16));
                        try {
                            Link l = new Link(address);
                            poll.add(l);
                        } catch (HotdogedException e3) {
                            e3.printStackTrace();
                        }
                    } catch (NumberFormatException e4) {
                        logger.l3("?LO file " + loname + " is invalid");
                        badFileList.add(file);
                    }
                    file.delete();
                    i++;
                } else {
                    try {
                        Utils.copyFileToExtMemory(file.getAbsolutePath(), EXT_DIR_SUFFIX, true);
                        logger.l3("Unknown file " + loname + " moved to external storage");
                        Notifier.INSTANSE.notify(new UnknownFileEvent(loname));
                    } catch (Exception e5) {
                        logger.l3(loname + " could not be saved, deleting. Reason: " + e5.getMessage());
                        file.delete();
                    }
                    i++;
                }
            }
            for (Link l2 : poll) {
                System.out.println("Poll to " + l2 + " needs to be created");
            }
        }
        return badFileList;
    }

    public void end() {
        if (!this.tossed.isEmpty()) {
            logger.l3("Messages wrote:");
            for (String area : this.tossed.keySet()) {
                logger.l3(String.format("\t%s - %d", area, this.tossed.get(area)));
            }
        }
        if (!this.bad.isEmpty()) {
            logger.l2("Messages dropped:");
            for (String area2 : this.bad.keySet()) {
                logger.l2(String.format("\t%s - %d", area2, this.bad.get(area2)));
            }
        }
        for (Link l : this.pollLinks) {
            if (FtnTools.getOptionBooleanDefFalse(l, LinkOption.BOOLEAN_CRASH_ECHOMAIL)) {
                System.out.println("Poll to " + l + " needs to be created");
            }
        }
    }

    public static List<Message> getMessagesForLink(Link link) {
        FtnAddress link_address = new FtnAddress(link.getLinkAddress());
        FtnAddress our_address = Main.info.getAddress();
        Ftn2D link2d = new Ftn2D(link_address.getNet(), link_address.getNode());
        Ftn2D our2d = new Ftn2D(our_address.getNet(), our_address.getNode());
        List<FtnMessage> messages = new ArrayList<>();
        List<File> attachedFiles = new ArrayList<>();
        List<Message> ret = new ArrayList<>();
        try {
            List<Netmail> netmails = link.getUnsentNetmail();
            if (!netmails.isEmpty()) {
                for (Netmail netmail : netmails) {
                    FtnMessage msg = FtnTools.netmailToFtnMessage(netmail);
                    messages.add(msg);
                    logger.l4(String.format("Pack netmail #%d %s\n%s\n -> %s for %s flags %d", netmail.getId(), netmail.getFromFTN(), netmail.getText(), netmail.getToFTN(), link.getLinkAddress(), Integer.valueOf(msg.getAttribute())));
                    if ((netmail.getAttr() & 16) > 0) {
                        String filename = netmail.getSubject().replaceAll("^[\\./\\\\]+", "_");
                        File file = new File(Main.getInbound() + File.separator + filename);
                        if (file.canRead()) {
                            attachedFiles.add(file);
                            logger.l5("Netmail with attached file " + filename);
                        }
                    }
                    netmail.setSend(true);
                    netmail.moveFromOutbound();
                }
            }
        } catch (Exception e) {
            logger.l2("Netmail error " + link.getLinkAddress(), e);
            Main.SystemInfo.getLogger().log(TAG, e.getMessage());
        }
        List<Echomail> toRemove = new ArrayList<>();
        List<EchomailAwaiting> mailToSend = link.getAwaitingEchomail();
        for (EchomailAwaiting ema : mailToSend) {
            Echomail mail = ema.getMail();
            Echoarea area = mail.getArea();
            toRemove.add(mail);
            Set<Ftn2D> seenby = new HashSet<>(FtnTools.read2D(mail.getSeenBy()));
            if (seenby.contains(link2d) && link_address.getPoint() == 0) {
                logger.l5(link2d + " is in seenby for " + link_address);
            } else {
                List<Ftn2D> path = FtnTools.read2D(mail.getPath());
                seenby.add(our2d);
                seenby.add(link2d);
                if (!path.contains(our2d)) {
                    path.add(our2d);
                }
                FtnAddress addr = Main.info.getAddress();
                Ftn2D d2 = new Ftn2D(addr.getNet(), addr.getNode());
                seenby.add(d2);
                FtnMessage message = new FtnMessage();
                message.setNetmail(false);
                message.setCodePage(mail.getCodePage());
                message.setArea(area.getName().toUpperCase());
                message.setFromName(mail.getFromName());
                message.setToName(mail.getToName());
                message.setFromAddr(our_address);
                message.setToAddr(link_address);
                message.setDate(mail.getDate());
                message.setSubject(mail.getSubject());
                message.setText(mail.getText());
                message.setSeenby(new ArrayList(seenby));
                message.setPath(path);
                logger.l4("Echomail #" + mail.getId() + " (" + area.getName() + ") packed for " + link.getLinkAddress());
                messages.add(message);
            }
        }
        for (Echomail echo : toRemove) {
            try {
                echo.moveFromOutbound();
            } catch (HotdogedException e2) {
                Main.SystemInfo.getLogger().log(TAG, e2.getMessage());
            }
        }
        if (!messages.isEmpty()) {
            FtnTools.pack(messages, link);
        }
        synchronized (FtnTosser.class) {
            File inbound = new File(Main.getInbound());
            File[] fileList = inbound.listFiles();
            if (fileList == null) {
                logger.l2("Error accessing inbound: not found " + Main.getInbound());
            } else {
                for (File file2 : fileList) {
                    String loname = file2.getName().toLowerCase();
                    if (loname.matches("^out_" + link.getId() + "\\..*$")) {
                        boolean packed = true;
                        try {
                            new ZipFile(file2).close();
                        } catch (Exception e3) {
                            packed = false;
                        }
                        try {
                            Message m = new Message(file2);
                            if (packed) {
                                m.setMessageName(FtnTools.generateEchoBundle());
                            } else {
                                m.setMessageName(FtnTools.generate8d() + ".pkt");
                            }
                            ret.add(m);
                        } catch (Exception e4) {
                        }
                    }
                }
            }
        }
        for (File f : attachedFiles) {
            try {
                ret.add(new Message(f));
                f.delete();
            } catch (Exception e5) {
                logger.l3("File attach failed " + f.getAbsolutePath());
            }
        }
        return ret;
    }
}
