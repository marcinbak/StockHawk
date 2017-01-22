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

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.provider.BaseColumns;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static com.udacity.stockhawk.ui.DetailsActivity.STOCK_CODE_EXTRA;

/**
 * Created by marcinbak on 22/01/2017.
 */
public class StockWidgetRemoteViewsService extends RemoteViewsService {

  private static final String[] STOCKS_COLUMNS = {
      BaseColumns._ID,
      Contract.Quote.COLUMN_SYMBOL,
      Contract.Quote.COLUMN_PRICE,
      Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
      Contract.Quote.COLUMN_PERCENTAGE_CHANGE
  };

  private static final int INDEX_ID         = 0;
  private static final int INDEX_SYMBOL     = 1;
  private static final int INDEX_PRICE      = 2;
  private static final int INDEX_CHANGE     = 3;
  private static final int INDEX_PERCENTAGE = 4;

  private DecimalFormat dollarFormatWithPlus;
  private DecimalFormat dollarFormat;
  private DecimalFormat percentageFormat;

  @Override
  public void onCreate() {
    super.onCreate();
    dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    dollarFormatWithPlus.setPositivePrefix("+$");
    percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
    percentageFormat.setMaximumFractionDigits(2);
    percentageFormat.setMinimumFractionDigits(2);
    percentageFormat.setPositivePrefix("+");
  }

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new RemoteViewsFactory() {
      private Cursor data = null;

      @Override
      public void onCreate() {
      }

      @Override
      public void onDataSetChanged() {
        if (data != null) {
          data.close();
        }
        final long identityToken = Binder.clearCallingIdentity();
        data = getContentResolver().query(Contract.Quote.URI,
            STOCKS_COLUMNS,
            null,
            null,
            Contract.Quote.COLUMN_SYMBOL + " ASC");
        Binder.restoreCallingIdentity(identityToken);
      }

      @Override
      public void onDestroy() {
        if (data != null) {
          data.close();
          data = null;
        }
      }

      @Override
      public int getCount() {
        return data == null ? 0 : data.getCount();
      }

      @Override
      public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
            data == null || !data.moveToPosition(position)) {
          return null;
        }
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
        String symbol = data.getString(INDEX_SYMBOL);
        float price = data.getFloat(INDEX_PRICE);
        float rawAbsoluteChange = data.getFloat(INDEX_CHANGE);

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(data.getFloat(INDEX_PERCENTAGE) / 100);

        String changeStr;
        if (PrefUtils.getDisplayMode(StockWidgetRemoteViewsService.this)
            .equals(StockWidgetRemoteViewsService.this.getString(R.string.pref_display_mode_absolute_key))) {
          changeStr = change;
        } else {
          changeStr = percentage;
        }

        views.setTextViewText(R.id.widget_symbol, symbol);
        views.setTextViewText(R.id.widget_price, dollarFormat.format(price));
        views.setTextViewText(R.id.widget_change, changeStr);

        if (rawAbsoluteChange > 0) {
          views.setTextColor(R.id.widget_change, StockWidgetRemoteViewsService.this.getResources().getColor(R.color.material_green_700));
        } else {
          views.setTextColor(R.id.widget_change, StockWidgetRemoteViewsService.this.getResources().getColor(R.color.material_red_700));
        }

        final Intent fillInIntent = new Intent();
        fillInIntent.putExtra(STOCK_CODE_EXTRA, symbol);
        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
        return views;
      }

      @Override
      public RemoteViews getLoadingView() {
        return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
      }

      @Override
      public int getViewTypeCount() {
        return 1;
      }

      @Override
      public long getItemId(int position) {
        if (data.moveToPosition(position)) {
          return data.getString(INDEX_SYMBOL).hashCode();
        }
        return position;
      }

      @Override
      public boolean hasStableIds() {
        return true;
      }
    };
  }

}
