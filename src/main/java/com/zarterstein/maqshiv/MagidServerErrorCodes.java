package com.zarterstein.maqshiv;

public class MagidServerErrorCodes {
    private MagidServerErrorCodes() {}
    public static final String GENERIC_ERROR = "E00000";

    // E01*: Server/file related
    public static final String TOO_MANY_SESSIONS = "E01000";
    public static final String SESSION_NOT_FOUND = "E01001";
    public static final String FILE_NOT_FOUND = "E01002";

    // E02*: Repository related
    public static final String STORY_NOT_FOUND = "E02001";

    // E09*: Client-related
    public static final String CLIENT_ID_NOT_PROVIDED = "E09000";
}
