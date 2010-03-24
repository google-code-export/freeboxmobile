package org.madprod.freeboxmobile.fax;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import org.madprod.freeboxmobile.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Activité de selection d'un fichier dans le dossier de la SDCard
 * @author Ludovic Meurillon
 */
public class FileChooserActivity extends Activity {

	private File currentDir = null;
	private ArrayAdapter<String> fileNameAdapter = null;
	private Button parentDirectoryButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.file_chooser);

		final ArrayAdapter<String> fileNameAdapter = getFileNameAdapter();
		final ListView files = (ListView) findViewById(R.id.fileList);
		files.setAdapter(fileNameAdapter);
		files.refreshDrawableState();
		files.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int arg2, long arg3) {
				onFileNameClicked(((TextView) view).getText().toString());
			}
		});
		
		parentDirectoryButton = (Button) findViewById(R.id.parentDirectoryButton);
		parentDirectoryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				goToParent();
			}
		});
		
		setCurrentDir(Environment.getExternalStorageDirectory());
		refreshFileNames();
	}

	/**
	 * Rafraichit la liste des noms de dossiers et fichiers pr�sents dans le repertoire courant
	 */
	private void refreshFileNames() {
		if (this.currentDir != null && currentDir.isDirectory()) {
			//Repertoires
			final File[] dirs = currentDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});

			//Fichiers pouvant être séléctionnés par l'activité
			final String[] fileNames = currentDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File parent, String filename) {
					//Extensions gérées par le FileChooser
					final String[] matchingExtensions = new String[]{".pdf",".jpg",".jpeg"};
					//Issue 134 : On compare dorénavant le filename en lettre minuscule
					final String lowercaseFilename = filename.toLowerCase();
					for(int i=0;i<matchingExtensions.length;i++){
						if(lowercaseFilename.toLowerCase().endsWith(matchingExtensions[i])){
							return true;
						}
					}
					return false;
				}
			});
			
			//Rafraichissement des vues
			final ArrayAdapter<String> fileNameAdapter = getFileNameAdapter();
			fileNameAdapter.clear();
			if (dirs != null) {
				for (File directory : dirs) {
					fileNameAdapter.add("/" + directory.getName());
				}
			}
			if (fileNames != null) {
				for (String fileName : fileNames) {
					if (new File(currentDir, fileName).isDirectory()) {
						fileNameAdapter.add("/" + fileName);
					} else {
						fileNameAdapter.add(fileName);
					}
				}
			}
		}
	}

	/*
	 * Gestion du bouton "back"
	 * (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isOnRootDirectory()) {
				//Fin de l'activite (appui sur "back" dans le dossier racine)
				finish();
				return true;
			} else {
				//Deplacement du filechooser dans le dossier parent
				goToParent();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Deplacement dans le dossier parent
	 */
	private final void goToParent() {
		setCurrentDir(currentDir.getParentFile());
		refreshFileNames();
	}

	/**
	 * Teste si le repertoire courant est le repertoire racine
	 * @return
	 */
	private final boolean isOnRootDirectory() {
		return currentDir.getAbsolutePath().equals(
				Environment.getExternalStorageDirectory().getAbsolutePath());
	}

	private final void setCurrentDir(File currentDir) {
		this.currentDir = currentDir;
		parentDirectoryButton.setEnabled(!isOnRootDirectory());
	}

	/**
	 * Selection d'un item de la liste
	 * @param fileNameClicked
	 */
	public void onFileNameClicked(String fileNameClicked) {
		if (currentDir != null) {
			if (fileNameClicked.startsWith("/")) {
				fileNameClicked = fileNameClicked.substring(1);
			}
			final File clickedFile = new File(currentDir, fileNameClicked);
			if (clickedFile.isDirectory()) {
				setCurrentDir(clickedFile);
				refreshFileNames();
			} else {
				//Un fichier a ete selectionne : fin de l'activite
				this.setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_PICK, Uri.fromFile(clickedFile)));
				this.finish();
			}
		}
	}

	private ArrayAdapter<String> getFileNameAdapter() {
		if (fileNameAdapter == null) {
			fileNameAdapter = new ArrayAdapter<String>(this
					.getApplicationContext(), R.layout.fax_files_row);
		}
		return fileNameAdapter;
	}
}
