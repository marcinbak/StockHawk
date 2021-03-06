package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailsActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidget extends AppWidgetProvider {

  void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                       int appWidgetId) {
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget_small);

    // Create an Intent to launch MainActivity
    Intent intent = new Intent(context, MainActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
    views.setOnClickPendingIntent(R.id.widget, pendingIntent);

    // Set up the collection
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      setRemoteAdapter(context, views);
    } else {
      setRemoteAdapterV11(context, views);
    }
    Intent clickIntentTemplate = new Intent(context, DetailsActivity.class);
    PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
        .addNextIntentWithParentStack(clickIntentTemplate)
        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
    views.setEmptyView(R.id.widget_list, R.id.widget_empty);

    // Tell the AppWidgetManager to perform an update on the current app widget
    appWidgetManager.updateAppWidget(appWidgetId, views);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // There may be multiple widgets active, so update all of them
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }
  }

  /**
   * Sets the remote adapter used to fill in the list items
   *
   * @param views RemoteViews to set the RemoteAdapter
   */
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(R.id.widget_list,
        new Intent(context, StockWidgetRemoteViewsService.class));
  }

  /**
   * Sets the remote adapter used to fill in the list items
   *
   * @param views RemoteViews to set the RemoteAdapter
   */
  @SuppressWarnings("deprecation")
  private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(0, R.id.widget_list,
        new Intent(context, StockWidgetRemoteViewsService.class));
  }
}

