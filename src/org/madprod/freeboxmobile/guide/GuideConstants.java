package org.madprod.freeboxmobile.guide;

import org.madprod.freeboxmobile.Constants;

import android.view.Menu;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public interface GuideConstants extends Constants
{
	static final String KEY_SPLASH_GUIDE = "splashscreen_guide";
	static final String KEY_SPLASH_FAVORIS = "splashscreen_favoris";
	static final String KEY_MODE = "mode";
	
    static final int GUIDE_OPTION_REFRESH = Menu.FIRST;
    static final int GUIDE_OPTION_SELECT = Menu.FIRST + 1;
    static final int GUIDE_OPTION_MODE = Menu.FIRST + 2;
    static final int GUIDE_OPTION_FILTER = Menu.FIRST + 3;
    
    static final int GUIDE_CONTEXT_ENREGISTRER = Menu.FIRST;
    static final int GUIDE_CONTEXT_DETAILS = Menu.FIRST + 1;
    
    static final String IMAGES_URL = "https://adsls.free.fr/im/chaines/";
    
    static final int FAVORIS_COMMAND_NONE = 0;
    static final int FAVORIS_COMMAND_RESET = 1;
    static final int FAVORIS_COMMAND_ADD = 2;
    static final int FAVORIS_COMMAND_SUPPR = 3;
    
    static final int DATA_NOT_DOWNLOADED = 0;
    static final int DATA_NEW_DATA = 1;
    static final int DATA_FROM_CACHE = 2;
    
    static final String genres[] = {
    	"",						// 0
    	"Film",					// 1
    	"Téléfilm",				// 2
    	"Série/Feuilleton",		// 3
    	"Feuilleton",			// 4
    	"Documentaire",			// 5
    	"Théatre",				// 6
    	"Opéra",				// 7
    	"Ballet",				// 8
    	"Variétés",				// 9
    	"Magazine",				// 10
    	"Jeunesse",				// 11
    	"Jeu",					// 12
    	"Musique",				// 13
    	"Divertissement",		// 14
    	"Court-métrage",		// 15
    	"Dessin animé",			// 16
    	"",						// 17
    	"",						// 18
    	"Sport",				// 19
    	"Journal",				// 20
    	"Information",			// 21
    	"Débat",				// 22
    	"Danse",				// 23
    	"Spectacle",			// 24
    	"Gala",					// 25
    	"Reportage",			// 26
    	"Fin des Emissions",	// 27
    	"",						// 28
    	"",						// 29
    	"",						// 30
    	"Emission religieuse",	// 31
    	""						// 32
    	};
    
    public class Categorie implements Comparable<Categorie>
    {
    	public String name;
    	public int id;
    	public boolean checked;
    	
    	@Override
    	public int compareTo(Categorie another)
    	{
    		return name.compareTo(another.name);
    	}
    }
}
