package fr.lepetitpingouin.android.t411;

public class Default {
    public static final String URL_FRIENDS = "http://www.t411.me/my/friends/";
    // Fr�quence de mise � jour par d�faut (en minutes)
    public static String UpdateFreq = "60";
    public static String timeout = "20";

    public static String URL_VERSIONS = "http://www.mediafire.com/folder/8961s22215ot3/t411-android";

    public static String Appwidget_flag_updating = "intent.WIDGET_FLAG_UPDATING";
    public static String Appwidget_update = "android.appwidget.action.APPWIDGET_UPDATE_t411";
    public static String Appwidget_clock_update = "intent.t411_CLOCK_UPDATE";

    public static String URL_INDEX = "http://www.t411.me";
    public static String URL_MAILS = "http://www.t411.me/users/login/?returnto=%2Fmailbox%2F";
    public static String URL_LOGIN = "http://www.t411.me/users/login/?returnto=%2Fusers%2Flogin";
    public static String URL_READMAIL = "http://www.t411.me/users/login/?returnto=%2Fmailbox%2Fmail%2F%3Fid%3D"; // + ID
    public static String URL_SEARCH = "http://www.t411.me/torrents/search/?search="; // + terms
    public static String URL_SAY_THANKS = "http://www.t411.me/users/login/?returnto=%2Ftorrents%2Fthanks%2F%3Fid%3D"; //+ ID
    public static String URL_GET_NFO = "http://www.t411.me/users/login/?returnto=%2Ftorrents%2Fnfo%2F%3Fid%3D"; //+ ID
    public static String URL_GET_PREZ = "http://www.t411.me/torrents/torrents/?id="; //+ ID
    public static String URL_GET_SUBCAT = "http://www.t411.me/torrents/search/?search=@@@@@&cat="; //+ ID
    public static String URL_GET_TORRENT = "http://www.t411.me/users/login/?returnto=%2Ftorrents%2Fdownload%2F%3Fid%3D"; //+ ID
    public static String URL_THREAD = "http://www.t411.me/forum.php#/discussion/35195";
    public static String URL_DONATE = "http://www.t411.me/bonus/";
    public static String URL_CREATE_ACCOUNT = "http://www.t411.me/users/signup/";
    public static String URL_BOOKMARKS = "http://www.t411.me/my/bookmarks/";
    public static String URL_BOOKMARK = "http://www.t411.me/users/login/?returnto=%2Fmy%2Fbookmarks%2F?add="; //+ ID
    public static String URL_UNBOOKMARK = "http://www.t411.me/my/bookmarks/"; // Authentification requise au préalable
    public static String URL_SEARCH_SAVE = "http://www.t411.me/my/search-save/?search="; //+ ID // Authentification requise au préalable
    public static String URL_SEARCH_GET = "http://www.t411.me/users/login/?returnto=%2Fmy%2Fsearch";
    public static String URL_UNFAVORITE = "http://www.t411.me/my/search-delete"; // Authentification requise au préalable
    public static String URL_SENDMAIL = "http://www.t411.me/mailbox/compose/"; // Authentification requise au préalable
    public static final String URL_STATS = "http://www.t411.me/users/login/?returnto=%2Fusers%2Fdaily-stats%2F?id=";
    public static String URL_UPLOADS = "http://www.t411.me/my/torrents/?order=added&type=desc";

    public static final String URL_OTHERAPPS = "https://play.google.com/store/search?q=fr.lepetitpingouin.android";

    public static String Intent_Update_News = "android.appwidget.action.UPDATE_NEWS";
    public static String Intent_Refresh_Newspaper = "android.appwidget.action.REFRESH_NEWSPAPER";

    public static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36";
}
