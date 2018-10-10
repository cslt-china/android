package com.google.android.apps.signalong.jsonentities;

import com.google.gson.annotations.SerializedName;

public class AgreementResponse extends BaseResponse {

  @SerializedName("data")
  private String url;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
