package com.udacity.stockhawk.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import org.parceler.Parcels;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends DialogFragment {
  private static final String ARG_HISTORY_ITEMS = "ARG_HISTORY_ITEMS";

  private List<StockHistoryModel> mHistoryItems;
  private Unbinder                unbinder;

  @BindView(R.id.chart) LineChart mChart;

  public ChartFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param historyItems Parameter 1.
   * @return A new instance of fragment ChartFragment.
   */
  public static ChartFragment newInstance(List<StockHistoryModel> historyItems) {
    ChartFragment fragment = new ChartFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_HISTORY_ITEMS, Parcels.wrap(historyItems));
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mHistoryItems = Parcels.unwrap(getArguments().getParcelable(ARG_HISTORY_ITEMS));
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_chart, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);
    setupChart();
  }

  private void setupChart() {
    XAxis xAxis = mChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
    xAxis.setAxisMinimum(0f);
    xAxis.setGranularity(1f);
    xAxis.setValueFormatter(new IAxisValueFormatter() {
      @Override
      public String getFormattedValue(float value, AxisBase axis) {
        return mHistoryItems.get((int) value).date;
      }
    });

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
    set.setColor(Color.DKGRAY);
    set.setLineWidth(2.5f);
    set.setFillColor(Color.DKGRAY);
    set.setMode(LineDataSet.Mode.LINEAR);
    set.setDrawValues(true);
    set.setValueTextSize(10f);
    set.setValueTextColor(Color.DKGRAY);

    set.setAxisDependency(YAxis.AxisDependency.LEFT);

    mChart.setData(linearData);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (unbinder != null) {
      unbinder.unbind();
    }
  }

  @Override
  @NonNull
  public Dialog onCreateDialog(final Bundle savedInstanceState) {

    // the content
    final RelativeLayout root = new RelativeLayout(getActivity());
    root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    // creating the fullscreen dialog
    final Dialog dialog = new Dialog(getActivity());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(root);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    return dialog;
  }
}
