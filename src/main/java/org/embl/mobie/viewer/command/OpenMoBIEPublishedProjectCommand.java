package org.embl.mobie.viewer.command;

import org.embl.mobie.viewer.MoBIE;
import org.embl.mobie.viewer.MoBIESettings;
import org.embl.mobie.viewer.project.PublishedProject;
import org.embl.mobie.viewer.project.PublishedProjectsCreator;
import ij.gui.GenericDialog;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import java.io.IOException;
import java.util.HashMap;


@Plugin(type = Command.class, menuPath = "Plugins>MoBIE>Open>Open Published MoBIE Project..." )
public class OpenMoBIEPublishedProjectCommand implements Command
{
	@Override
	public void run()
	{
		selectProject();
	}

	private void selectProject()
	{
		final HashMap< String, PublishedProject> projects = new PublishedProjectsCreator().getPublishedProjects();

		final GenericDialog gd = new GenericDialog( "Please select a project" );

		final String[] items = ( String[] ) projects.keySet().toArray( new String[ projects.size() ]);
		gd.addChoice( "Project", items, items[ 0 ] );
		gd.showDialog();
		if ( gd.wasCanceled() ) return;
		final String choice = gd.getNextChoice();

		final PublishedProject project = projects.get( choice );

		try
		{
			new MoBIE( project.location, MoBIESettings.settings().publicationURL( project.pulicationURL ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	public static void main( String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		ij.command().run( OpenMoBIEPublishedProjectCommand.class, true );
	}
}
