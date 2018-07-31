package com.google.android.apps.cslt.ui.data_collection;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.apps.cslt.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class StandardDataFragment extends Fragment {


    @BindView(R.id.from_thesaurus_btn)
    Button fromThesaurusBtn;
    @BindView(R.id.define_name_btn)
    Button defineNameBtn;

    Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_standard_data, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.from_thesaurus_btn, R.id.define_name_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.from_thesaurus_btn:
                //TODO(zhichongh): Fill from_thesaurus_btn button click handler here.
                break;
            case R.id.define_name_btn:
                //TODO(zhichongh): Fill define_name_btn button click handler here.
                break;
                default:
        }
    }
}
