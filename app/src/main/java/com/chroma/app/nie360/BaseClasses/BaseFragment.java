package com.chroma.app.nie360.BaseClasses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;


public class BaseFragment extends Fragment {



  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


  }

  public void addFragmentWithBackstack(int containerId, Fragment fragment, String tag){
    getActivity().getSupportFragmentManager().beginTransaction().
            add(containerId,fragment,tag)
            .addToBackStack(tag).commit();
  }

  public void addFragment(int containerId, Fragment fragment, String tag){
    getActivity().getSupportFragmentManager().beginTransaction()
            .add(containerId,fragment,tag).commit();
  }

  public void replaceFragmentWithBackstack(int containerId, Fragment fragment, String tag) {
    getActivity().getSupportFragmentManager().beginTransaction().
            replace(containerId, fragment, tag)
            .addToBackStack(tag).commit();
  }

  public void replaceFragment(int containerId, Fragment fragment, String tag){
    getActivity().getSupportFragmentManager().beginTransaction()
            .replace(containerId,fragment,tag).commit();
  }

  public Boolean isConnected() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService( Context.CONNECTIVITY_SERVICE);
    if (connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
            connectivityManager.getNetworkInfo( ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
      //we are connected to a network
      return true;
    } else {
      return false;
    }
  }

}
