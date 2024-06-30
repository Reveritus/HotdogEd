package com.pushkin.hotdoged.export;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: FileAttachment.kt */
@Metadata(bv = {1, 0, 1}, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003¢\u0006\u0002\u0010\u0006J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\t\u0010\f\u001a\u00020\u0003HÆ\u0003J\t\u0010\r\u001a\u00020\u0003HÆ\u0003J'\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003HÆ\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\u0012\u001a\u00020\u0013HÖ\u0001J\t\u0010\u0014\u001a\u00020\u0003HÖ\u0001R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0005\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\n\u0010\b¨\u0006\u0015"}, d2 = {"Lcom/pushkin/hotdoged/export/FileAttachment;", "", "localFilePath", "", "remoteFileName", "param", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getLocalFilePath", "()Ljava/lang/String;", "getParam", "getRemoteFileName", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "HotdogEdCommon_release"}, k = 1, mv = {1, 1, 5})
public final /* data */ class FileAttachment {
    @NotNull
    private final String localFilePath;
    @NotNull
    private final String param;
    @NotNull
    private final String remoteFileName;

    @NotNull
    public static /* bridge */ /* synthetic */ FileAttachment copy$default(FileAttachment fileAttachment, String str, String str2, String str3, int i, Object obj) {
        if ((i & 1) != 0) {
            str = fileAttachment.localFilePath;
        }
        if ((i & 2) != 0) {
            str2 = fileAttachment.remoteFileName;
        }
        if ((i & 4) != 0) {
            str3 = fileAttachment.param;
        }
        return fileAttachment.copy(str, str2, str3);
    }

    @NotNull
    /* renamed from: component1  reason: from getter */
    public final String getLocalFilePath() {
        return this.localFilePath;
    }

    @NotNull
    /* renamed from: component2  reason: from getter */
    public final String getRemoteFileName() {
        return this.remoteFileName;
    }

    @NotNull
    /* renamed from: component3  reason: from getter */
    public final String getParam() {
        return this.param;
    }

    @NotNull
    public final FileAttachment copy(@NotNull String localFilePath, @NotNull String remoteFileName, @NotNull String param) {
        Intrinsics.checkParameterIsNotNull(localFilePath, "localFilePath");
        Intrinsics.checkParameterIsNotNull(remoteFileName, "remoteFileName");
        Intrinsics.checkParameterIsNotNull(param, "param");
        return new FileAttachment(localFilePath, remoteFileName, param);
    }

    public boolean equals(Object other) {
        if (this != other) {
            if (other instanceof FileAttachment) {
                FileAttachment fileAttachment = (FileAttachment) other;
                if (!Intrinsics.areEqual(this.localFilePath, fileAttachment.localFilePath) || !Intrinsics.areEqual(this.remoteFileName, fileAttachment.remoteFileName) || !Intrinsics.areEqual(this.param, fileAttachment.param)) {
                }
            }
            return false;
        }
        return true;
    }

    public int hashCode() {
        String str = this.localFilePath;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        String str2 = this.remoteFileName;
        int hashCode2 = ((str2 != null ? str2.hashCode() : 0) + hashCode) * 31;
        String str3 = this.param;
        return hashCode2 + (str3 != null ? str3.hashCode() : 0);
    }

    public String toString() {
        return "FileAttachment(localFilePath=" + this.localFilePath + ", remoteFileName=" + this.remoteFileName + ", param=" + this.param + ")";
    }

    public FileAttachment(@NotNull String localFilePath, @NotNull String remoteFileName, @NotNull String param) {
        Intrinsics.checkParameterIsNotNull(localFilePath, "localFilePath");
        Intrinsics.checkParameterIsNotNull(remoteFileName, "remoteFileName");
        Intrinsics.checkParameterIsNotNull(param, "param");
        this.localFilePath = localFilePath;
        this.remoteFileName = remoteFileName;
        this.param = param;
    }

    @NotNull
    public final String getLocalFilePath() {
        return this.localFilePath;
    }

    @NotNull
    public final String getParam() {
        return this.param;
    }

    @NotNull
    public final String getRemoteFileName() {
        return this.remoteFileName;
    }
}
