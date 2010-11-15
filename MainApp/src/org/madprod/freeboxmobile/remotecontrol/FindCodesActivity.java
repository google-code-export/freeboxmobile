package org.madprod.freeboxmobile.remotecontrol;

import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FindCodesActivity extends Activity {
	
	private final int[] imgs = new int[]{
			R.drawable.interface_free,
			R.drawable.parametre_free,
			R.drawable.code
	};
	private final String[] descs = new String[]{
			"Sur votre TV, accèdez au menu de la freebox en appuyant sur la touche Free de votre télécommande",
			"Ensuite, allez dans le menu Informations Générales",
			"Le code se trouve dans cet écran et est composé de 8 chiffres"
	};
	private int index = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.didact);
		
		Button previousB = (Button)findViewById(R.id.previous);
		previousB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				previous();
			}
		});
		Button nextB = (Button)findViewById(R.id.next);
		nextB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				next();
			}
		});
		if (index <= 0){
			previousB.setVisibility(View.INVISIBLE);
		}
		ImageView iv = (ImageView)findViewById(R.id.ImageViewDidact);
		iv.setImageResource(imgs[index]);			
		TextView tv = (TextView)findViewById(R.id.TextViewDidact);
		tv.setText(descs[index]);
		
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	private void next(){
		index ++;
		if (index < imgs.length){
			ImageView iv = (ImageView)findViewById(R.id.ImageViewDidact);
			iv.setImageResource(imgs[index]);			
			TextView tv = (TextView)findViewById(R.id.TextViewDidact);
			tv.setText(descs[index]);
		}
		if (index > 0){
			Button previousB = (Button)findViewById(R.id.previous);		
			previousB.setVisibility(View.VISIBLE);
		}
		if (index == imgs.length-1){
			Button nextB = (Button)findViewById(R.id.next);
			nextB.setText("Finir");
		}
		if (index == imgs.length){
			finish();
		}
		
	}

	private void previous(){
		index--;
		if (index >= 0){
			ImageView iv = (ImageView)findViewById(R.id.ImageViewDidact);
			iv.setImageResource(imgs[index]);			
			TextView tv = (TextView)findViewById(R.id.TextViewDidact);
			tv.setText(descs[index]);
		}
		if (index <= 0){
			Button previousB = (Button)findViewById(R.id.previous);		
			previousB.setVisibility(View.INVISIBLE);
		}
		if (index < imgs.length-1){
			Button nextB = (Button)findViewById(R.id.next);
			nextB.setText("Suivant");
		}
	}
}
