package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;

/**
 * Created by meyergre on 17/06/13.
 */
class Ratio {
    String ratio;
    private SharedPreferences prefs;
    private Context context;

    public Ratio(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public int getTitleColor() {

        if (prefs.getString("classe", "???").contains("Power Seeder"))
            return ContextCompat.getColor(this.context,R.color.t411_purple);
        if (prefs.getString("classe", "???").contains("Uploader"))
            return ContextCompat.getColor(this.context, R.color.t411_gold);
        if (prefs.getString("classe", "???").contains("Team Pending"))
            return ContextCompat.getColor(this.context, R.color.t411_grey);
        if (prefs.getString("classe", "???").contains("Modérateur"))
            return ContextCompat.getColor(this.context, R.color.t411_black);
        if (prefs.getString("classe", "???").contains("Super Modérateur"))
            return ContextCompat.getColor(this.context, R.color.t411_darkred);
        if (prefs.getString("classe", "???").contains("Administrateur"))
            return ContextCompat.getColor(this.context, R.color.t411_salmon);
        else
            return ContextCompat.getColor(this.context, R.color.t411_blue);
    }


    public int getSmiley() {
        String ratio = prefs.getString("lastRatio", "0.00");
        //String upload = new BSize(prefs.getString("lastUpload", "0.00 MB")).convert();
        BSize _upload = new BSize(prefs.getString("lastUpload", "0.00 MB"));

        double numRatio = Double.valueOf(ratio.replace(",","."));

        if (numRatio == 0.00)
            return R.drawable.smiley_angel;

        // easter egg :) si le ratio contient 42, on affiche Marvin :)
        if (String.valueOf(numRatio).contains("42"))
            return R.drawable.smiley_marvin;
        // easter egg :) si le ratio est de 13.37, on affiche
        // l'invadroid
        if (numRatio == 13.37)
            return R.drawable.smiley_leet;

        // easter egg :) si le ratio est de 6.66, on affiche le diable
        if (numRatio == 6.66)
            return R.drawable.smiley_devil;

        // easter egg :) si le ratio est de 4.04, on affiche le smiley introuvable
        if (numRatio == 4.04)
            return R.drawable.smiley_not_found;

        // easter egg :) si le ratio est de 3.14, on affiche le smiley Pi
        if (numRatio == 3.14)
            return R.drawable.smiley_pi;

        // easter egg :) si on est le 24/12 ou le 25/12, on affiche le pere
        // noel-droid
        if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER
                && ((Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 25) || (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 24)))
            return R.drawable.smiley_xmas;

        if (numRatio < 0.75)
            return R.drawable.smiley_cry;

        if (numRatio < 1)
            return R.drawable.smiley_neutral;

        if (numRatio < 2)
            return R.drawable.smiley_good;

        // Ratio a 50 ou +, et plus de 100 GB en UP, Chuck Norris :)
        if ((numRatio > 49.99) && (_upload.getInGB() > 250))
            return R.drawable.smiley_chucknorris;

        if (numRatio > 9.99)
            return R.drawable.smiley_love;

        if (numRatio > 1.99)
            return R.drawable.smiley_w00t;

        return R.drawable.smiley_unknown;
    }

    public int getSmiley(Double numRatio) {

        if (numRatio == 0.00)
            return R.drawable.smiley_angel;

        if (numRatio < 0.75)
            return R.drawable.smiley_cry;

        if (numRatio < 1)
            return R.drawable.smiley_neutral;

        if (numRatio < 2)
            return R.drawable.smiley_good;

        if (numRatio > 9.99)
            return R.drawable.smiley_love;

        if (numRatio > 1.99)
            return R.drawable.smiley_w00t;

        return R.drawable.smiley_unknown;
    }
}
