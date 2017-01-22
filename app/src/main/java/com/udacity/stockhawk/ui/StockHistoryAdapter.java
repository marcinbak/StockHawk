package com.udacity.stockhawk.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

class StockHistoryAdapter extends RecyclerView.Adapter<StockHistoryAdapter.StockHistoryViewHolder> {

  private final Context                    context;
  private final DecimalFormat              dollarFormatWithPlus;
  private final DecimalFormat              dollarFormat;
  private final DecimalFormat              percentageFormat;
  private final String                     stockCode;
  private       List<StockHistoryModel>    data;

  StockHistoryAdapter(Context context, String stockCode) {
    this.context = context;
    this.stockCode = stockCode;

    dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    dollarFormatWithPlus.setPositivePrefix("+$");
    percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
    percentageFormat.setMaximumFractionDigits(2);
    percentageFormat.setMinimumFractionDigits(2);
    percentageFormat.setPositivePrefix("+");
  }

  void setData(List<StockHistoryModel> data) {
    this.data = data;
    notifyDataSetChanged();
  }

  @Override
  public StockHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);

    return new StockHistoryViewHolder(item);
  }

  @Override
  public void onBindViewHolder(StockHistoryViewHolder holder, int position) {
    StockHistoryModel dayData = data.get(position);

    holder.date.setText(dayData.date);
    holder.price.setText(dollarFormat.format(dayData.closeValue));

    if (dayData.changeValue > 0) {
      holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
    } else {
      holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
    }

    String change = dollarFormatWithPlus.format(dayData.changeValue);
    String percentage = percentageFormat.format(dayData.changePercentage);

    if (PrefUtils.getDisplayMode(context)
        .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
      holder.change.setText(change);
    } else {
      holder.change.setText(percentage);
    }
  }

  @Override
  public int getItemCount() {
    int count = 0;
    if (data != null) {
      count = data.size();
    }
    return count;
  }

  public List<StockHistoryModel> getData() {
    return data;
  }

  class StockHistoryViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.symbol) TextView date;
    @BindView(R.id.price)  TextView price;
    @BindView(R.id.change) TextView change;

    StockHistoryViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

}
