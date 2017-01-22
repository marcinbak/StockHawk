package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_HISTORY;
import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_SYMBOL;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
    SwipeRefreshLayout.OnRefreshListener {

  public final static String STOCK_CODE_EXTRA = "STOCK_CODE_EXTRA";

  public static void start(Activity context, @NonNull String stockCode) {
    Intent intent = new Intent(context, DetailsActivity.class);
    intent.putExtra(STOCK_CODE_EXTRA, stockCode);
    context.startActivity(intent);
  }

  private static final int STOCK_HISTORY_LOADER = 1;
  @SuppressWarnings("WeakerAccess")
  @BindView(R.id.recycler_view)
  RecyclerView       stockRecyclerView;
  @SuppressWarnings("WeakerAccess")
  @BindView(R.id.swipe_refresh)
  SwipeRefreshLayout swipeRefreshLayout;
  @SuppressWarnings("WeakerAccess")
  @BindView(R.id.error)
  TextView           error;

  private StockHistoryAdapter adapter;
  private String              stockCode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_details);
    ButterKnife.bind(this);

    stockCode = getIntent().getStringExtra(STOCK_CODE_EXTRA);

    adapter = new StockHistoryAdapter(this, stockCode);
    stockRecyclerView.setAdapter(adapter);
    stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    swipeRefreshLayout.setOnRefreshListener(this);
    swipeRefreshLayout.setRefreshing(true);
    onRefresh();

    QuoteSyncJob.initialize(this);
    getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);
  }

  private boolean networkUp() {
    ConnectivityManager cm =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnectedOrConnecting();
  }

  @Override
  public void onRefresh() {
    QuoteSyncJob.syncImmediately(this);

    if (!networkUp() && adapter.getItemCount() == 0) {
      swipeRefreshLayout.setRefreshing(false);
      error.setText(getString(R.string.error_no_network));
      error.setVisibility(View.VISIBLE);
    } else if (!networkUp()) {
      swipeRefreshLayout.setRefreshing(false);
      Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
    } else if (PrefUtils.getStocks(this).size() == 0) {
      swipeRefreshLayout.setRefreshing(false);
      error.setText(getString(R.string.error_no_stocks));
      error.setVisibility(View.VISIBLE);
    } else {
      error.setVisibility(View.GONE);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(this,
        Contract.Quote.URI,
        new String[]{COLUMN_HISTORY},
        COLUMN_SYMBOL + " = ?", new String[]{stockCode}, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    swipeRefreshLayout.setRefreshing(false);

    if (data.getCount() != 0) {
      error.setVisibility(View.GONE);
    }

    data.moveToFirst();
    int colIdx = data.getColumnIndex(COLUMN_HISTORY);
    String[] history = data.getString(colIdx).split("\n");

    List<StockHistoryModel> historyList = new ArrayList<>(history.length);
    for (String item : history) {
      String[] dayData = item.split(", ");

      if (dayData.length != 3) {
//      error
        return;
      }

      float open = new BigDecimal(dayData[1]).floatValue();
      float close = new BigDecimal(dayData[2]).floatValue();
      float diff = close - open;
      long dateMillis = Long.parseLong(dayData[0]);
      String myString = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(dateMillis));

      float percentageChange = diff / close;
      historyList.add(new StockHistoryModel(dateMillis, myString, close, diff, percentageChange));
    }

    adapter.setData(historyList);
  }


  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    swipeRefreshLayout.setRefreshing(false);
    adapter.setData(null);
  }


  private void setDisplayModeMenuItemIcon(MenuItem item) {
    if (PrefUtils.getDisplayMode(this)
        .equals(getString(R.string.pref_display_mode_absolute_key))) {
      item.setIcon(R.drawable.ic_percentage);
    } else {
      item.setIcon(R.drawable.ic_dollar);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.details_activity_settings, menu);
    MenuItem item = menu.findItem(R.id.action_change_units);
    setDisplayModeMenuItemIcon(item);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_change_units) {
      PrefUtils.toggleDisplayMode(this);
      setDisplayModeMenuItemIcon(item);
      adapter.notifyDataSetChanged();
      return true;
    } else if (id == R.id.action_show_graph) {
      openGraphFragment();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void openGraphFragment() {
    List<StockHistoryModel> list = adapter.getData();
    List<StockHistoryModel> newList = new ArrayList<>(list.size());
    for (StockHistoryModel item : list) {
      newList.add(0, item);
    }
    DialogFragment fragment = ChartFragment.newInstance(newList);
    fragment.show(getSupportFragmentManager(), ChartFragment.class.getName());
  }
}
