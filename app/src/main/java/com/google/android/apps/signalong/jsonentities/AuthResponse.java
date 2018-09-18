package com.google.android.apps.signalong.jsonentities;

/**
 * The AuthResponse class is the authenticate response body. This is autogenerated by running GSON
 * format plugin from Android Studio.
 */
public class AuthResponse {

  private int code;
  private String message;
  private DataBean data;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public DataBean getData() {
    return data;
  }

  public void setData(DataBean data) {
    this.data = data;
  }

  /** The DataBean class is real data, that contains access token and refresh token. */
  public static class DataBean {

    private String refresh;
    private String access;

    public String getRefresh() {
      return refresh;
    }

    public void setRefresh(String refresh) {
      this.refresh = refresh;
    }

    public String getAccess() {
      return access;
    }

    public void setAccess(String access) {
      this.access = access;
    }
  }
}
