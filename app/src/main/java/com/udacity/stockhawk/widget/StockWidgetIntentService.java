/*
 * (c) Neofonie Mobile GmbH (2017)
 *
 * This computer program is the sole property of Neofonie Mobile GmbH (http://mobile.neofonie.de)
 * and is protected under the German Copyright Act (paragraph 69a UrhG).
 *
 * All rights are reserved. Making copies, duplicating, modifying, using or distributing
 * this computer program in any form, without prior written consent of Neofonie Mobile GmbH, is prohibited.
 * Violation of copyright is punishable under the German Copyright Act (paragraph 106 UrhG).
 *
 * Removing this copyright statement is also a violation.
 */
package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;
import com.udacity.stockhawk.data.Contract;

/**
 * Created by marcinbak on 22/01/2017.
 */
public class StockWidgetIntentService extends IntentService {

  private static final String[] STOCKS_COLUMNS   = {
      BaseColumns._ID,
      Contract.Quote.COLUMN_SYMBOL,
      Contract.Quote.COLUMN_PRICE,
      Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
      Contract.Quote.COLUMN_PERCENTAGE_CHANGE
  };
  // these indices must match the projection
  private static final int      INDEX_ID         = 0;
  private static final int      INDEX_SYMBOL     = 1;
  private static final int      INDEX_PRICE      = 2;
  private static final int      INDEX_CHANGE     = 3;
  private static final int      INDEX_PERCENTAGE = 4;

  /**
   * Creates an IntentService.  Invoked by your subclass's constructor.
   */
  public StockWidgetIntentService() {
    super("StockWidgetIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // Retrieve all of the Today widget ids: these are the widgets we need to update
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
        StockWidget.class));

    Cursor data = getContentResolver().query(Contract.Quote.URI, STOCKS_COLUMNS, null,
        null, Contract.Quote.COLUMN_SYMBOL + " ASC");
    if (data == null) {
      return;
    }
    if (!data.moveToFirst()) {
      data.close();
      return;
    }

    String symbol = data.getString(INDEX_SYMBOL);
    float price = data.getFloat(INDEX_PRICE);
    float change = data.getFloat(INDEX_CHANGE);

    data.close();

    // Perform this loop procedure for each Today widget
//    for (int appWidgetId : appWidgetIds) {
//      int layoutId = R.layout.widget_today_small;
//      RemoteViews views = new RemoteViews(getPackageName(), layoutId);
//
//      views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
//
//      // Create an Intent to launch MainActivity
//      Intent launchIntent = new Intent(this, MainActivity.class);
//      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
//      views.setOnClickPendingIntent(R.id.widget, pendingIntent);
//
//      // Tell the AppWidgetManager to perform an update on the current app widget
//      appWidgetManager.updateAppWidget(appWidgetId, views);
//    }
  }

}
