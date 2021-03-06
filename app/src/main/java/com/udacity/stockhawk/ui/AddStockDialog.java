package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.udacity.stockhawk.R;


public class AddStockDialog extends DialogFragment {

  @SuppressWarnings("WeakerAccess")
  @BindView(R.id.dialog_stock)
  EditText stock;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    LayoutInflater inflater = LayoutInflater.from(getActivity());
    @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

    ButterKnife.bind(this, custom);

    InputFilter filter = new InputFilter() {
      public CharSequence filter(CharSequence source, int start, int end,
                                 Spanned dest, int dstart, int dend) {
        for (int i = start; i < end; i++) {
          if (Character.isWhitespace(source.charAt(i))) {
            return "";
          }
        }
        return null;
      }
    };

    stock.setFilters(new InputFilter[]{filter});
    stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        addStock();
        return true;
      }
    });
    builder.setView(custom);

    builder.setMessage(getString(R.string.dialog_title));
    builder.setPositiveButton(getString(R.string.dialog_add),
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            addStock();
          }
        });
    builder.setNegativeButton(getString(R.string.dialog_cancel), null);

    Dialog dialog = builder.create();

    Window window = dialog.getWindow();
    if (window != null) {
      window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    return dialog;
  }

  private void addStock() {
    Activity parent = getActivity();
    if (parent instanceof MainActivity) {
      ((MainActivity) parent).addStock(stock.getText().toString());
    }
    dismissAllowingStateLoss();
  }


}
