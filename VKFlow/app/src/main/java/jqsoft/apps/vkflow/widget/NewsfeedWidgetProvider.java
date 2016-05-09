package jqsoft.apps.vkflow.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.widget.RemoteViews;

import jqsoft.apps.vkflow.MainActivity;
import jqsoft.apps.vkflow.R;

public class NewsfeedWidgetProvider extends AppWidgetProvider {
    @TargetApi(VERSION_CODES.JELLY_BEAN)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            final RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.appwidget_newsfeed);

            final Intent intent = new Intent(context, NewsfeedWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.lvNewsfeedList, intent);
            views.setEmptyView(R.id.lvNewsfeedList, R.id.tvEmpty);

            Intent templateIntent = new Intent(context, MainActivity.class);
            templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            templateIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent templatePendingIntent = PendingIntent.getActivity(context, 0,
                    templateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.lvNewsfeedList, templatePendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
