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
package com.udacity.stockhawk.ui;

import org.parceler.Parcel;

/**
 * Created by marcinbak on 22/01/2017.
 */
@Parcel
public class StockHistoryModel {

  public StockHistoryModel() {
  }

  public StockHistoryModel(long dateMillis, String date, float closeValue, float changeValue, float changePercentage) {
    this.dateMillis = dateMillis;
    this.date = date;
    this.closeValue = closeValue;
    this.changeValue = changeValue;
    this.changePercentage = changePercentage;
  }

  long dateMillis;
  String date;
  float  closeValue;
  float  changeValue;
  float  changePercentage;
}
