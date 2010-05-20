package org.madprod.freeboxmobile.remotecontrol;

import java.util.ArrayList; 
import java.util.HashMap;

import org.madprod.freeboxmobile.FBMNetTask;
import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author Clement Beslon
 * $Id: RemoteControlActivity.java 2010-05-04 $
 * 
 */

public class RemoteControlPreferences extends Activity
{
	private final ArrayList<PreferencesInfos> boitiers = new ArrayList<PreferencesInfos>();
	private SharedPreferences preferences;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		FBMNetTask.register(this);

		setContentView(R.layout.remotecontrol_prefs);
		setTitle(getString(R.string.app_name)+" - Gestion des boitiers");

		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		HashMap<String, Boolean> adresses = CommandManager.getAdresses();
		int nbBoitiers = 0;
		for (String adresse : adresses.keySet()){
			if (adresses.get(adresse)){
				nbBoitiers++;
				String code = preferences.getString(adresse.trim()+"_code", "Code");
				Boolean state = preferences.getBoolean(adresse.trim()+"_state", false);
				boitiers.add(new PreferencesInfos(adresse, nbBoitiers, code, state));				
			}
		}



		ListView lv = (ListView)findViewById(R.id.ListPreferencesRemoteControls);
		lv.setAdapter(new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null){
					convertView = getLayoutInflater().inflate(R.layout.remotecontrol_prefsline, null);
				}

				TextView label = (TextView)convertView.findViewById(R.id.remotecontrol_label);
				label.setText(boitiers.get(position).toString());
				EditText code = (EditText)convertView.findViewById(R.id.remotecontrol_code);
				code.setText(boitiers.get(position).getCode());
				CheckBox state = (CheckBox)convertView.findViewById(R.id.remotecontrol_active);
				state.setChecked(boitiers.get(position).isActivate());



				return convertView;
			}


			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int arg0) {
				return null;
			}

			@Override
			public int getCount() {
				return boitiers.size();
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 100, 0, "Sauvegarder");
		menu.add(0, 101, 0, "Annuler");
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 100:
			ListView lv = (ListView)findViewById(R.id.ListPreferencesRemoteControls);

			Editor editor = preferences.edit();

			for (int i=0; i<lv.getChildCount(); i++){
				View v = lv.getChildAt(i);
				v.findViewById(R.id.remotecontrol_code);
				String code = ((EditText)v.findViewById(R.id.remotecontrol_code)).getText().toString();
				Boolean checked = ((CheckBox)v.findViewById(R.id.remotecontrol_active)).isChecked();

				if (checked && (code.length()==0 || !code.matches("[0-9]+"))){
					Toast.makeText(getApplicationContext(), "Code non correct", Toast.LENGTH_SHORT).show();
					editor.putBoolean(boitiers.get(i).getAdresse()+"_state", false);
				}else{
					editor.putBoolean(boitiers.get(i).getAdresse()+"_state", checked);
				}
				editor.putString(boitiers.get(i).getAdresse()+"_code", code);

			}

			editor.commit();
			break;
		case 101:
			finish();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	public static class PreferencesInfos{
		private final String adresse;
		private final int num;
		private final String code;
		private final Boolean activate;

		public PreferencesInfos(String _adresse, int _num, String _code, Boolean _activate) {
			adresse = _adresse;
			num = _num;
			code = _code;
			activate = _activate;
		}

		public String getAdresse() {
			return adresse;
		}
		public String getCode() {
			return code;
		}
		public int getNum() {
			return num;
		}
		public Boolean isActivate() {
			return activate;
		}

		@Override
		public String toString() {
			return "Boitier n "+num+" ( "+adresse+" )";
		}

	}


	@Override
	public void onDestroy()
	{
		FBMNetTask.unregister(this);
		super.onDestroy();
	}



}