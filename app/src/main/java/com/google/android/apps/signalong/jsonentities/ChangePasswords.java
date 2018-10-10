package com.google.android.apps.signalong.jsonentities;

import com.google.gson.annotations.SerializedName;

public class ChangePasswords {

  @SerializedName("old_password")
  private String oldPassword;

  @SerializedName("new_password")
  private String newPassword;

  public ChangePasswords(String oldPassword, String newPassword) {
    this.oldPassword = oldPassword;
    this.newPassword = newPassword;
  }

  public String getOldPassword() {
    return oldPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

}
