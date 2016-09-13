package fr.lepetitpingouin.android.t411;

/**
 * Created by meyergre on 08/06/13.
 */
class CategoryIcon {

    private String catCode;

    public CategoryIcon(String category) {
        this.catCode = category;
    }

    public static boolean isPr0n(int icon) {
        return (icon == R.drawable.ic_new_xxx || icon == R.drawable.ic_new_t411);
    }

    public int getIcon() {

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
