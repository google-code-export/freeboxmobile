package org.madprod.freeboxmobile.guide;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

public class Favoris implements Comparable<Favoris>
{
	public int guidechaine_id;
	public int canal;
	public String name;
	public String image;
	
	@Override
	public int compareTo(Favoris another)
	{
		return (canal - another.canal);
	}
}
