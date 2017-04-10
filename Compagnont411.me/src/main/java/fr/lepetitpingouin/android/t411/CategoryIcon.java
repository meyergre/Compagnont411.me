package fr.lepetitpingouin.android.t411;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by meyergre on 08/06/13.
 */
class CategoryIcon {

    private String catCode;

    public CategoryIcon(String category) {
        this.catCode = category;
    }

    public static boolean isPrOn(int icon) {
        return icon == R.drawable.ic_new_xxx || icon == R.drawable.ic_new_t411;
    }

    public static JSONObject getHardcodedCategories() {
        JSONObject json = new JSONObject();

        try {
            json = new JSONObject("{ '395': {id: '395', name:'Audio', cats: { '400': {id: '400', name: 'Karaoké'}, '623': {id: '623', name: 'Musique'}, '403': {id: '403', name: 'Samples'}, '642': {id: '642', name: 'Podcasts Radio'} }}, '404': {id: '404', name: 'eBook', cats: {'405': {id: '405', name: 'Audio'}, '406': {id: '406', name: 'BDs'}, '407': {id: '407', name: 'Comics'}, '408': {id: '408', name: 'Livres'}, '409': {id: '409', name: 'Mangas'}, '410': {id: '410', name: 'Presse'} }}, '340': {id: '340', name: 'Emulation', cats: {'342': {id: '342', name: 'Emulateurs'}, '344': {id: '344', name: 'Roms'} }}, '624': {id: '624', name: 'Jeu Vidéo', cats: {'239': {id: '239', name: 'Linux'}, '245': {id: '245', name: 'MacOS'}, '246': {id: '246', name: 'Windows'}, '309': {id: '309', name: 'Microsoft'}, '307': {id: '307', name: 'Nintendo'}, '308': {id: '308', name: 'Sony'}, '626': {id: '626', name: 'Smartphone'}, '628': {id: '628', name: 'Tablette'}, '630': {id: '630', name: 'Autre'} }}, '392': {id: '392', name: 'GPS', cats: {'391': {id: '391', name: 'Applications'}, '393': {id: '393', name: 'Cartes'}, '394': {id: '394', name: 'Divers'} }}, '233': {id: '233', name: 'Application', cats: {'234': {id: '234', name: 'Linux'}, '235': {id: '235', name: 'MacOS'}, '236': {id: '236', name: 'Windows'}, '625': {id: '625', name: 'Smartphone'}, '627': {id: '627', name: 'Tablette'}, '638': {id: '638', name: 'Formation'}, '629': {id: '629', name: 'Autre'} }}, '210': {id: '210', name: 'Film/Video', cats: {'455': {id: '455', name: 'Animation'}, '637': {id: '637', name: 'Animation Série'}, '633': {id: '633', name: 'Concert'}, '634': {id: '634', name: 'Documentaire'}, '639': {id: '639', name: 'Emission TV'}, '631': {id: '631', name: 'Films'}, '433': {id: '433', name: 'Série TV'}, '635': {id: '635', name: 'Spectacle'}, '636': {id: '636', name: 'Sport'}, '402': {id: '402', name: 'Vidéo-clips'} }}, '456': {id: '456', name: 'xXx', cats: {'461': {id: '461', name: 'eBooks'}, '462': {id: '462', name: 'Jeux vidéo'}, '632': {id: '632', name: 'Vidéo'}, '641': {id: '641', name: 'Animation'} }} }");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public int getIcon() {

        if(catCode.isEmpty()) return R.drawable.ic_new_t411;

        //Musique
        if ("395, 623, 400, 403, 642".contains(catCode))
            return R.drawable.ic_new_music;

        //Ebooks
        if ("404, 405, 406, 407, 408, 409, 410".contains(catCode))
            return R.drawable.ic_new_ebook;

        //Emulation
        if ("340, 342, 344".contains(catCode))
            return R.drawable.ic_new_emulation;

        //Applications
        if ("233, 234, 235, 236, 625, 627, 629, 638".contains(catCode))
            return R.drawable.ic_new_mobile;

        //Jeu vidéo
        if ("624, 246, 239, 245, 309, 307, 308, 626, 628, 630".contains(catCode))
            return R.drawable.ic_new_game;

        //Animation
        if ("455, 637".contains(catCode))
            return R.drawable.ic_new_animation;

        //Film Video
        if ("433, 210, 633, 634, 631, 635, 636, 639, 402, 433".contains(catCode))
            return R.drawable.ic_new_film;

        //GPS
        if ("392, 391, 393, 394".contains(catCode))
            return R.drawable.ic_new_gps;

        //XXX
        if ("456, 632, 461, 462, 641".contains(catCode))
            return R.drawable.ic_new_xxx;

        return R.drawable.ic_new_t411;
    }
}
