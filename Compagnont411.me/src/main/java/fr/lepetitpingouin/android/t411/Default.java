package fr.lepetitpingouin.android.t411;

class Default {
    public static final String IP_T411 = "t411.ai";

    public static final String API_T411 = "https://api."+IP_T411;
    public static final String URL_API_GET_TORRENT = "/torrents/download/"; // + ID
    public static final String URL_API_BOOKMARKS = "/bookmarks";
    public static final String URL_API_BOOKMARK_TORRENT = "/bookmarks/save/"; // + ID
    public static final String URL_API_BOOKMARK_DELETE = "/bookmarks/delete/"; // + ID

    public static final int SHOW_ASSISTANT_FOR_VERSION_UNDER = 168;

    public static final String T411_TOP_100 = "http://"+IP_T411+"/top/100/";
    public static final String T411_TOP_TODAY = "http://"+IP_T411+"/top/today/";
    public static final String T411_TOP_WEEK = "http://"+IP_T411+"/top/week/";
    public static final String T411_TOP_MONTH = "http://"+IP_T411+"/top/month/";

    public static final String API_T411_TOP_100 = API_T411+"/torrents/top/100";
    public static final String API_T411_TOP_TODAY = API_T411+"/torrents/top/today";
    public static final String API_T411_TOP_WEEK = API_T411+"/torrents/top/week";
    public static final String API_T411_TOP_MONTH = API_T411+"/torrents/top/month";


    public static final String URL_STATS = "http://"+IP_T411+"/users/daily-stats/?id="; // + user ID

    public static final String URL_FRIENDS = "http://"+IP_T411+"/my/friends/";
    public static final String URL_CHATI = "http://"+IP_T411+"/chati/index.php?room=support";
    public static final String URL_MESSAGE_DEL = "http://"+IP_T411+"/mailbox/delete/?id=";//+ID
    public static final String URL_MESSAGE_ARC = "http://"+IP_T411+"/mailbox/archive/?id=";//+ID
    public static final String URL_OTHERAPPS = "http://play.google.com/store/search?q=pub:Gr%C3%A9gory+Meyer";
    public static final String URL_FRIENDPROFILE = "http://"+IP_T411+"/users/profile/?id=";



    // Fr�quence de mise � jour par d�faut (en minutes)
    public static String UpdateFreq = "60";
    public static String timeout = "25";
    public static String URL_VERSIONS = "http://www.mediafire.com/folder/8961s22215ot3/t411-android";
    public static String Appwidget_flag_updating = "intent.WIDGET_FLAG_UPDATING";
    public static String Appwidget_update = "android.appwidget.action.APPWIDGET_UPDATE_t411";
    public static String Appwidget_clock_update = "intent.t411_CLOCK_UPDATE";
    public static String URL_INDEX = "http://"+IP_T411+"";
    public static String URL_USERPROFILE = "http://"+IP_T411+"/users/profile/";
    public static String URL_MAILS = "http://"+IP_T411+"/mailbox/";
    public static String URL_LOGIN = "http://"+IP_T411+"/users/login/?returnto=%2Fusers%2Flogin%2F";
    //public static String URL_LOGIN = "http://"+IP_T411+"/users/auth/";//?returnto=%2Fusers%2Fprofile%2F";
    public static String URL_LOGOUT = "http://"+IP_T411+"/users/logout/";
    public static String URL_SEARCH = "http://"+IP_T411+"/torrents/search/?name="; // + terms
    public static String URL_SAY_THANKS = "http://"+IP_T411+"/torrents/thanks/?id="; //+ ID
    public static String URL_SHARE = "http://"+IP_T411+"/t/"; //+ ID
    public static String URL_MESSAGE = "http://"+IP_T411+"/mailbox/mail/?id=";//+ID
    ///chati/index.php
    public static String URL_SHOUTBOX = "http://"+IP_T411+"/users/login/?returnto=%2Fchati%2F"; //+ ID
    // Messagerie //
    public static String MSG_SEPARATOR = "\\s*.*Message.*origine.*\\S";
    public static String MSG_REPLACEMENT = "<div class='msg_origine'><i>Message précédent :</i><br/>";
    public static String URL_GET_PREZ = "http://"+IP_T411+"/torrents/torrents/?id="; //+ ID
    public static String URL_GET_SUBCAT = "http://"+IP_T411+"/torrents/search/?search=@@@@@&cat="; //+ ID
    public static String URL_GET_TORRENT = "http://"+IP_T411+"/torrents/download/?id="; //+ ID
    public static String URL_THREAD = "http://"+IP_T411+"/forum.php#/discussion/35195";
    public static String URL_DONATE = "http://"+IP_T411+"/bonus/";
    public static String URL_CREATE_ACCOUNT = "http://"+IP_T411+"/users/signup/";
    public static String URL_BOOKMARKS = "http://"+IP_T411+"/my/bookmarks/";
    public static String URL_BOOKMARK = "http://"+IP_T411+"/my/bookmarks/?add="; //+ ID
    public static String URL_UNBOOKMARK = "http://"+IP_T411+"/my/bookmarks/"; // Authentification requise au préalable
    public static String URL_SEARCH_SAVE = "http://"+IP_T411+"/my/search-save/"; //+ ID // Authentification requise au préalable
    public static String URL_SEARCH_GET = "http://"+IP_T411+"/my/search/";
    public static String URL_UNFAVORITE = "http://"+IP_T411+"/my/search-delete/"; // Authentification requise au préalable
    public static String URL_SENDMAIL = "http://"+IP_T411+"/mailbox/compose/"; // Authentification requise au préalable
    public static String URL_UPLOADS = "http://"+IP_T411+"/my/torrents/?order=added&type=desc";
    public static String Intent_Update_News = "android.appwidget.action.UPDATE_NEWS";
    public static String Intent_Refresh_Newspaper = "android.appwidget.action.REFRESH_NEWSPAPER";

    //public static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36";
    public static String USER_AGENT = "Android (Custom browser) WebKit/Based Compagnon/t411";

    public static String BITCOIN_ADDRESS = "1Mp5oQy5BR4gvDkdZW5RUtpP3HTUcaZcBC";
}
