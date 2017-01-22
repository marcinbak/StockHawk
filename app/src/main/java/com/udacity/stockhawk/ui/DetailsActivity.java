package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_HISTORY;
import static com.udacity.stockhawk.data.Contract.Quote.COLUMN_SYMBOL;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

  public final static String STOCK_CODE_EXTRA = "STOCK_CODE_EXTRA";

  public static void start(Activity context, @NonNull String stockCode) {
    Intent intent = new Intent(context, DetailsActivity.class);
    intent.putExtra(STOCK_CODE_EXTRA, stockCode);
    context.startActivity(intent);
  }

  private static final int STOCK_HISTORY_LOADER = 1;

  private List<StockHistoryModel> mHistoryItems;

  @BindView(R.id.chart) LineChart mChart;

  private String stockCode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);
    ButterKnife.bind(this);

    stockCode = getIntent().getStringExtra(STOCK_CODE_EXTRA);

    QuoteSyncJob.initialize(this);
    getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);

    setupChart();
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

    mHistoryItems = historyList;
    setChartData();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mChart.setData(null);
  }

  private void setupChart() {
    XAxis xAxis = mChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setAxisMinimum(0f);
    xAxis.setGranularity(1f);
    xAxis.setTextColor(Color.WHITE);
    xAxis.setValueFormatter(new IAxisValueFormatter() {
      @Override
      public String getFormattedValue(float value, AxisBase axis) {
        return mHistoryItems.get((int) value).date;
      }
    });

    mChart.getAxisLeft().setTextColor(Color.WHITE);
    mChart.getAxisRight().setTextColor(Color.WHITE);
    mChart.getLegend().setTextColor(Color.WHITE);
  }

  private void setChartData() {
    List<Entry> entries = new ArrayList<>(mHistoryItems.size());

    int counter = 0;
    for (StockHistoryModel item : mHistoryItems) {
      Entry e = new Entry(counter, item.closeValue);
      entries.add(e);
      counter++;
    }

    LineDataSet set = new LineDataSet(entries, getString(R.string.stock_history));
    LineData linearData = new LineData(set);
    set.setDrawCircles(false);
    set.setColor(Color.WHITE);
    set.setLineWidth(2.5f);
    set.setFillColor(Color.WHITE);
    set.setMode(LineDataSet.Mode.LINEAR);
    set.setDrawValues(true);
    set.setValueTextSize(10f);
    set.setValueTextColor(Color.WHITE);

    set.setAxisDependency(YAxis.AxisDependency.LEFT);

    mChart.setData(linearData);
  }
}
