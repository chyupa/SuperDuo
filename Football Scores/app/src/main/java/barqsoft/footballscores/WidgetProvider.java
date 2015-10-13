package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chyupa on 24-Sep-15.
 */
public class WidgetProvider extends AppWidgetProvider {

    private Cursor cursor;
    private final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL,
    };

    private final int ID = 0;
    private final int HOME_NAME = 1;
    private final int AWAY_NAME = 2;
    private final int HOME_GOALS = 3;
    private final int AWAY_GOLAS = 4;
    private final int TIME = 5;

    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        final int N = appWidgetIds.length;

        for( int i = 0; i < N; i++ ){
            int appWidgetId = appWidgetIds[i];
//            String number = String.format("%03d", (new Random().nextInt(900) + 100));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
//            remoteViews.setTextViewText(R.id.textView, number);

            Date firstDate = new Date(System.currentTimeMillis()+((-2)*86400000));
            SimpleDateFormat firstDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date lastDate = new Date(System.currentTimeMillis()+(2*86400000));
            SimpleDateFormat lastDateFormat = new SimpleDateFormat("yyyy-MM-dd");

//            String[] dates =  {firstDateFormat.format(firstDate), lastDateFormat.format(lastDate) };

            Uri uri = DatabaseContract.scores_table.buildScoreWithDateRange();
            cursor = context.getContentResolver().query(uri, SCORES_COLUMNS, null, new String[] {firstDateFormat.format(firstDate), lastDateFormat.format(lastDate)}, "random() limit 1");
            if(cursor.getCount() < 1){
                Log.i("DB SCORE", "nothing found");
            }else{
                while (cursor.moveToNext()){
                    remoteViews.setTextViewText(R.id.home_name, cursor.getString(HOME_NAME));
                    remoteViews.setTextViewText(R.id.away_name, cursor.getString(AWAY_NAME));

                    String scoreText = Utilies.getScores(cursor.getInt(HOME_GOALS), cursor.getInt(AWAY_GOLAS));
                    remoteViews.setTextViewText(R.id.score_textview, scoreText);

                    remoteViews.setTextViewText(R.id.data_textview, cursor.getString(TIME) );

                    remoteViews.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(cursor.getString(HOME_NAME)));
                    remoteViews.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(cursor.getString(AWAY_NAME)));
                }
                cursor.close();
            }

            Intent intent = new Intent(context, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.prev_score, getPendingSelfIntent(context, "prev"));
//            remoteViews.setOnClickPendingIntent(R.id.next_score, getPendingSelfIntent(context, "next"));
            remoteViews.setOnClickPendingIntent(R.id.refresh, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }
    }
}
