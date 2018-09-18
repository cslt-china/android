package com.google.android.apps.signalong.jsonentities;

import com.google.gson.annotations.SerializedName;

/** The Token class is treated as an entity with valid time and user. */
public class Token {
  @SerializedName("token_Type")
  private String tokenType;

  @SerializedName("exp")
  private long tokenExpirationTimestamp;

  @SerializedName("jti")
  private String jsonTokenId;

  @SerializedName("user_id")
  private int userId;

  public long getTokenExpirationTimestamp() {
    return tokenExpirationTimestamp;
  }

  public void setTokenExpirationTimestamp(long tokenExpirationTimestamp) {
    this.tokenExpirationTimestamp = tokenExpirationTimestamp;
  }

  public String getJsonTokenId() {
    return jsonTokenId;
  }

  public void setJsonTokenId(String jsonTokenId) {
    this.jsonTokenId = jsonTokenId;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }
}
