package com.pushkin.ftn;

public class LinkOption {
    public static final String BOOLEAN_AREAFIX = "AreaFix";
    public static final String BOOLEAN_AUTOCREATE_AREA = "AreaAutoCreate";
    public static final String BOOLEAN_AUTOCREATE_FILE = "FileAutoCreate";
    public static final String BOOLEAN_CRASH_ECHOMAIL = "CrashEchomail";
    public static final String BOOLEAN_CRASH_FILEMAIL = "CrashFilemail";
    public static final String BOOLEAN_CRASH_NETMAIL = "CrashNetmail";
    public static final String BOOLEAN_FILEFIX = "FileFix";
    public static final String BOOLEAN_IGNORE_PKTPWD = "IgnorePktPwd";
    public static final String BOOLEAN_PACK_ECHOMAIL = "PackEchomail";
    public static final String BOOLEAN_PACK_NETMAIL = "PackNetmail";
    public static final String BOOLEAN_POLL_BY_TIMEOT = "PollByTimeout";
    public static final String LONG_LINK_LEVEL = "Level";
    public static final String SARRAY_LINK_GROUPS = "Groups";
    public static final String STRING_AREAFIX_PWD = "AreaFixPwd";
    public static final String STRING_FILEFIX_PWD = "FileFixPwd";
    private Link link;
    private String option;
    private String value;

    public LinkOption(Link link, String option) {
        this.link = link;
        this.option = option;
        this.value = link.getLinkOptionValue(option);
    }

    public Link getLink() {
        return this.link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public String getOption() {
        return this.option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return "LinkOptions [link=" + this.link + ", option=" + this.option + ", value=" + this.value + "]";
    }
}
