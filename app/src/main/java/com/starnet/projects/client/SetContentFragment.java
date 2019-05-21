package com.starnet.projects.client;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SetContentFragment extends Fragment {
    private View view;
    private EditText contentEditText;
    private CheckBox fromLeftToRightCheckBox;
    private CheckBox fromRightToLeftCheckBox;
    private Button submitButton;

    public static SetContentFragment newInstance(String content) {
        SetContentFragment fragment = new SetContentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_set_content, container, false);
        init();
        return view;
    }

    private void init() {
        contentEditText = view.findViewById(R.id.et_fragment_set_content);
        fromLeftToRightCheckBox = view.findViewById(R.id.fragment_from_left_to_right);
        fromRightToLeftCheckBox = view.findViewById(R.id.fragment_from_right_to_left);
        submitButton = view.findViewById(R.id.bt_fragment_submit_content);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contentEditText.getText().toString().equals("")){
                    Toast.makeText(getActivity().getApplicationContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
                }else if(!fromLeftToRightCheckBox.isChecked()&&!fromRightToLeftCheckBox.isChecked()){
                    Toast.makeText(getActivity().getApplicationContext(), "请选择滚动方式", Toast.LENGTH_SHORT).show();
                }else if(fromRightToLeftCheckBox.isChecked()&&fromLeftToRightCheckBox.isChecked()){
                    Toast.makeText(getActivity().getApplicationContext(), "只能选择一种滚动方式", Toast.LENGTH_SHORT).show();
                }
                else{
                    String rollType = "";
                    LCDClientActivity lcdClientActivity = (LCDClientActivity) getActivity();
                    if(fromLeftToRightCheckBox.isChecked()){
                        rollType = "0";
                    }else if(fromRightToLeftCheckBox.isChecked()){
                        rollType = "1";
                    }
                    lcdClientActivity.getConnectService().sendMessage("103,"+encoder(contentEditText.getText().toString()+","+rollType));
                }
            }
        });
    }

    private String encoder(String message){
        return Base64.encodeToString(message.getBytes(),Base64.DEFAULT);
    }
}
