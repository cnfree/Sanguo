
package com.actuate.development.tool.wizard;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.sf.feeling.swt.win32.extension.Win32;
import org.sf.feeling.swt.win32.extension.shell.Windows;
import org.sf.feeling.swt.win32.internal.extension.util.ImageCache;

import com.actuate.development.tool.model.IPortalViewerData;
import com.actuate.development.tool.model.InstallBRDProData;
import com.actuate.development.tool.model.Module;
import com.actuate.development.tool.model.Modules;
import com.actuate.development.tool.model.PathConfig;
import com.actuate.development.tool.model.ToolFeature;
import com.actuate.development.tool.model.ToolFeatureData;
import com.actuate.development.tool.task.CloneWorkspaceSettings;
import com.actuate.development.tool.task.InstallBRDPro;
import com.actuate.development.tool.task.SyncIPortalWorkspace;
import com.actuate.development.tool.util.LogUtil;
import com.actuate.development.tool.util.UIUtil;

public class ToolkitWizard extends Wizard
{

	private static final String MAXIMIZED = "Maximized";

	private static final String HEIGHT = "Height";

	private static final String WIDTH = "Width";

	private static final String POS_Y = "PosY";

	private static final String POS_X = "PosX";

	public static final String PROJECT = "Project";

	public static final String CURRENT_BRDPRO_PROJECT = "CurrentBRDProProject";

	public static final String CURRENT_IV_PROJECT = "CurrentIVProject";

	public static final String DIRECTORY = "Directory";

	public static final String MODULES = "Modules";

	public static final String CLEARDIRECTORY = "ClearDirectory";

	public static final String CLOSEBRDPRO = "CloseBRDPro";

	public static final String CREATESHORTCUT = "CreateShortcut";

	public static final String SHORTCUTARGUMENTS = "ShortcutArguments";

	private static final String DIALOG_SETTING_FILE = new File( System.getProperties( )
			.getProperty( "user.home" )
			+ "\\.brdpro_toolkit\\userInfo.xml" ).getAbsolutePath( );

	private static final String P4ROOT = "P4Root";

	private static final String P4VIEW = "P4View";

	private static final String CUSTOMPROJECTNAME = "CustomProjectName";

	private static final String FORCEOPERATION = "ForceOperation";

	private static final String REVERTFILES = "RevertFiles";

	private static final String P4SERVER = "P4Server";

	private static final String P4USER = "P4User";

	private static final String P4PASSWORD = "P4Password";

	private static final String P4CLIENT = "P4Client";

	// the model object.
	private ToolFeatureData data = new ToolFeatureData( );

	public ToolkitWizard( )
	{
		setWindowTitle( "Actuate BRDPro Development Toolkit" );
		setNeedsProgressMonitor( true );

		final DialogSettings dialogSettings = new DialogSettings( "userInfo" );
		try
		{
			if ( new File( DIALOG_SETTING_FILE ).exists( ) )
				dialogSettings.load( DIALOG_SETTING_FILE );

		}
		catch ( IOException e )
		{
			LogUtil.recordErrorMsg( e, false );
		}

		setDialogSettings( dialogSettings );

		IDialogSettings[] settings = getDialogSettings( ).getSections( );
		if ( settings != null )
		{
			for ( IDialogSettings setting : settings )
			{
				InstallBRDProData installData = new InstallBRDProData( );
				data.addInstallBRDProData( installData );
				installData.setProject( setting.getName( ) );
				if ( setting.get( DIRECTORY ) != null )
				{
					installData.setDirectory( setting.get( DIRECTORY ) );
				}
				if ( setting.get( MODULES ) != null
						&& setting.get( MODULES ).trim( ).length( ) > 0 )
				{
					String[] moduleNames = setting.get( MODULES ).split( ";" );
					Module[] modules = new Module[moduleNames.length];
					for ( int i = 0; i < modules.length; i++ )
					{
						modules[i] = Modules.getInstance( )
								.valueOf( moduleNames[i] );
					}
					installData.setModules( modules );
				}
				if ( setting.get( CLEARDIRECTORY ) != null )
				{
					installData.setNotClearDirectory( setting.getBoolean( CLEARDIRECTORY ) );
				}
				if ( setting.get( CLOSEBRDPRO ) != null )
				{
					installData.setNotCloseBRDPro( setting.getBoolean( CLOSEBRDPRO ) );
				}
				if ( setting.get( SHORTCUTARGUMENTS ) != null )
				{
					installData.setShortcutArguments( setting.get( SHORTCUTARGUMENTS ) );
				}
				if ( setting.get( CREATESHORTCUT ) != null )
				{
					installData.setNotCreateShortcut( setting.getBoolean( CREATESHORTCUT ) );
				}

				IPortalViewerData ivData = new IPortalViewerData( );
				ivData.setProject( setting.getName( ) );
				if ( setting.get( P4ROOT ) != null )
				{
					ivData.setRoot( setting.get( P4ROOT ) );
				}
				if ( setting.get( P4VIEW ) != null )
				{
					ivData.setView( setting.get( P4VIEW ) );
				}
				if ( setting.get( CUSTOMPROJECTNAME ) != null )
				{
					ivData.setCustomProjectName( setting.get( CUSTOMPROJECTNAME ) );
				}
				if ( setting.get( FORCEOPERATION ) != null )
				{
					ivData.setForceOperation( setting.getBoolean( FORCEOPERATION ) );
				}
				if ( setting.get( REVERTFILES ) != null )
				{
					ivData.setRevertFiles( setting.getBoolean( REVERTFILES ) );
				}
				if ( setting.get( P4SERVER ) != null )
				{
					ivData.setServer( setting.get( P4SERVER ) );
				}
				if ( setting.get( P4USER ) != null )
				{
					ivData.setUser( setting.get( P4USER ) );
				}
				if ( setting.get( P4PASSWORD ) != null )
				{
					ivData.setPassword( setting.get( P4PASSWORD ) );
				}
				if ( setting.get( P4CLIENT ) != null )
				{
					ivData.setClient( setting.get( P4CLIENT ) );
				}
				data.addIPortalViewerData( ivData );
			}
		}

		if ( getDialogSettings( ).get( CURRENT_BRDPRO_PROJECT ) != null )
		{
			data.setCurrentBRDProProject( getDialogSettings( ).get( CURRENT_BRDPRO_PROJECT ) );
		}

		if ( getDialogSettings( ).get( CURRENT_IV_PROJECT ) != null )
		{
			data.setCurrentIVProject( getDialogSettings( ).get( CURRENT_IV_PROJECT ) );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages( )
	{
		initShell( );

		addPage( new ToolFeaturePage( data ) );
		addPage( new CloneWorkspaceSettingsPage( data ) );
		addPage( new BRDProProjectPage( data ) );
		addPage( new BRDProSettingPage( data ) );
		addPage( new BRDProShortcutPage( data ) );
		addPage( new IPortalViewerProjectPage( data ) );
	}

	private void initShell( )
	{
		UIUtil.setShell( this.getShell( ) );
		Windows.setWindowStyle( this.getShell( ).handle,
				Windows.getWindowStyle( this.getShell( ).handle )
						| Win32.WS_MINIMIZEBOX );
		this.getShell( ).setImages( new Image[]{
				ImageCache.getImage( "/icons/actuate_16.png" ),
				ImageCache.getImage( "/icons/actuate_32.png" ),
				ImageCache.getImage( "/icons/actuate_48.png" )
		} );
		if ( getDialogSettings( ).get( WIDTH ) != null
				&& getDialogSettings( ).get( HEIGHT ) != null )
			this.getShell( ).setSize( getDialogSettings( ).getInt( WIDTH ),
					getDialogSettings( ).getInt( HEIGHT ) );
		else
		{
			this.getShell( ).setSize( 700, 600 );
		}
		if ( getDialogSettings( ).get( POS_X ) != null
				&& getDialogSettings( ).get( POS_Y ) != null )
			this.getShell( ).setLocation( getDialogSettings( ).getInt( POS_X ),
					getDialogSettings( ).getInt( POS_Y ) );
		else
		{
			int width = this.getShell( ).getMonitor( ).getClientArea( ).width;
			int height = this.getShell( ).getMonitor( ).getClientArea( ).height;
			int x = this.getShell( ).getSize( ).x;
			int y = this.getShell( ).getSize( ).y;
			this.getShell( )
					.setLocation( ( width - x ) / 2, ( height - y ) / 2 );
		}
		if ( getDialogSettings( ).get( MAXIMIZED ) != null )
			this.getShell( )
					.setMaximized( getDialogSettings( ).getBoolean( MAXIMIZED ) );

		this.getShell( ).addControlListener( new ControlListener( ) {

			public void controlMoved( ControlEvent e )
			{
				collectShellInfos( );

			}

			public void controlResized( ControlEvent e )
			{
				collectShellInfos( );
			}

			private void collectShellInfos( )
			{
				if ( !ToolkitWizard.this.getShell( ).getMaximized( )
						&& !ToolkitWizard.this.getShell( ).getMinimized( )
						&& ToolkitWizard.this.getShell( ).getVisible( ) )
				{
					Point location = ToolkitWizard.this.getShell( )
							.getLocation( );
					Point size = ToolkitWizard.this.getShell( ).getSize( );
					getDialogSettings( ).put( POS_X, location.x );
					getDialogSettings( ).put( POS_Y, location.y );
					getDialogSettings( ).put( WIDTH, size.x );
					getDialogSettings( ).put( HEIGHT, size.y );
				}

				if ( ToolkitWizard.this.getShell( ).getMaximized( ) )
				{
					getDialogSettings( ).put( MAXIMIZED, true );
				}
				else
				{
					getDialogSettings( ).put( MAXIMIZED, false );
				}
			}

		} );

		this.getShell( ).addDisposeListener( new DisposeListener( ) {

			public void widgetDisposed( DisposeEvent e )
			{
				saveToolkitSettings( );
			}

		} );

		this.getShell( ).addShellListener( new ShellAdapter( ) {

			public void shellActivated( ShellEvent e )
			{
				Windows.flashWindow( getShell( ).handle, false );
			}

			public void shellDeiconified( ShellEvent e )
			{
				Windows.flashWindow( getShell( ).handle, false );
			}

		} );

		try
		{
			ProgressMonitorDialog dialog = new ProgressMonitorDialog( null ) {

				protected void configureShell( Shell shell )
				{
					super.configureShell( shell );
					shell.setImages( new Image[]{
							ImageCache.getImage( "/icons/actuate_16.png" ),
							ImageCache.getImage( "/icons/actuate_32.png" ),
							ImageCache.getImage( "/icons/actuate_48.png" )
					} );
					shell.forceActive( );
				}
			};
			
			dialog.run( true, true, new IRunnableWithProgress( ) {

				public void run( final IProgressMonitor monitor )
						throws InvocationTargetException, InterruptedException
				{
					monitor.beginTask( "Collecting Actuate Build Projects...",
							IProgressMonitor.UNKNOWN );
					final String[][] fileNames = new String[1][];

					String buildDir = PathConfig.getProperty( PathConfig.ACTUATE_BUILD_DIR,
							"\\\\qaant\\ActuateBuild" );
					File file = new File( buildDir );
					fileNames[0] = file.list( new FilenameFilter( ) {

						public boolean accept( File dir, String name )
						{
							File file = new File( dir, name );
							monitor.subTask( "Scanning "
									+ file.getAbsolutePath( ) );
							List<File> brdproFiles = new ArrayList<File>( );
							List<File> iportalViewerFiles = new ArrayList<File>( );

							checkActuateBuildFile( file,
									brdproFiles,
									iportalViewerFiles );

							if ( brdproFiles.size( ) > 0 )
							{
								if ( !data.getBrdproMap( ).containsKey( name ) )
									data.getBrdproMap( ).put( name,
											new ArrayList<File>( ) );
								data.getBrdproMap( )
										.get( name )
										.addAll( brdproFiles );
							}
							if ( iportalViewerFiles.size( ) > 0
									&& Modules.getInstance( )
											.getIPortalProjects( )
											.contains( name ) )
							{
								if ( !data.getIportalViewMap( )
										.containsKey( name ) )
									data.getIportalViewMap( ).put( name,
											new ArrayList<File>( ) );
								data.getIportalViewMap( )
										.get( name )
										.addAll( iportalViewerFiles );
							}
							if ( brdproFiles.size( ) > 0
									|| iportalViewerFiles.size( ) > 0 )
								return true;
							return false;
						}

					} );

					monitor.done( );
				}
			} );
		}
		catch ( Exception e )
		{
			LogUtil.recordErrorMsg( e, false );
		}
	}

	private void checkActuateBuildFile( File file,
			final List<File> brdproFiles, final List<File> iportalViewerFiles )
	{
		final String[] subDirRegexs = PathConfig.getProperty( PathConfig.ACTUATE_BUILD_SUB_DIR,
				"(?i)ActuateBirtViewer;(?i)BRDPro" )
				.split( ";" );
		final String[] brdProRegexs = PathConfig.getProperty( PathConfig.BRDPRO,
				"(?i)actuatebirtdesignerprofessional.*\\.zip;(?i)brdpro.*\\.zip" )
				.split( ";" );
		final String[] iPortalRegexs = PathConfig.getProperty( PathConfig.IPORTAL,
				"(?i)actuatebirtviewer.*\\.zip;(?i)wl_tomcat_actuatebirtjavacomponent.*\\.war" )
				.split( ";" );

		File[] children = file.listFiles( new FileFilter( ) {

			public boolean accept( File file )
			{
				String fileName = file.getName( ).toLowerCase( );
				if ( file.isFile( ) )
				{
					for ( int i = 0; i < brdProRegexs.length; i++ )
					{
						if ( fileName.matches( brdProRegexs[i] ) )
						{
							brdproFiles.add( file );
							return false;
						}
					}

					for ( int i = 0; i < iPortalRegexs.length; i++ )
					{
						if ( fileName.matches( iPortalRegexs[i] ) )
						{
							iportalViewerFiles.add( file );
							return false;
						}
					}
				}
				else
				{
					for ( int i = 0; i < subDirRegexs.length; i++ )
					{
						if ( fileName.matches( subDirRegexs[i] ) )
							return true;
					}
				}
				return false;
			}

		} );

		if ( children != null )
		{
			for ( int i = 0; i < children.length; i++ )
			{
				checkActuateBuildFile( children[i],
						brdproFiles,
						iportalViewerFiles );
			}
		}
	}

	public IWizardPage getNextPage( IWizardPage page )
	{
		List<IWizardPage> pages = Arrays.asList( this.getPages( ) );
		int index = pages.indexOf( page );
		if ( index == pages.size( ) - 1 || index == -1 )
		{
			return null;
		}
		if ( index == 0 )
		{
			if ( data.getToolFeature( ) == ToolFeature.installBRDPro )
				return pages.get( 2 );
			else if ( data.getToolFeature( ) == ToolFeature.synciPortalWorkspace )
				return pages.get( 5 );
			else
				return pages.get( 1 );
		}
		else if ( index == 1 )
			return null;
		else if ( index == 4 )
			return null;
		else
			return (IWizardPage) pages.get( index + 1 );
	}

	public boolean performFinish( )
	{
		if ( getDialogSettings( ) != null )
		{
			saveToolkitSettings( );
		}

		try
		{
			// puts the data into a database ...
			getContainer( ).run( true, true, new IRunnableWithProgress( ) {

				public void run( IProgressMonitor monitor )
						throws InvocationTargetException, InterruptedException
				{

					if ( data != null )
					{
						if ( data.getToolFeature( ) == ToolFeature.installBRDPro )
						{
							new InstallBRDPro( data.getCurrentInstallBRDProData( ) ).execute( monitor );
						}
						else if ( data.getToolFeature( ) == ToolFeature.cloneWorkspaceSettings )
						{
							new CloneWorkspaceSettings( data ).execute( monitor );
						}
						else if ( data.getToolFeature( ) == ToolFeature.synciPortalWorkspace )
						{
							new SyncIPortalWorkspace( data.getCurrentIportalViewerData( ) ).execute( monitor );
						}
					}
					monitor.done( );
				}
			} );
		}
		catch ( Exception e )
		{
			LogUtil.recordErrorMsg( e, true );
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performCancel()
	 */
	public boolean performCancel( )
	{
		boolean ans = MessageDialog.openConfirm( getShell( ),
				"Confirmation",
				"Are you sure to cancel the task?" );
		if ( ans )
			return true;
		else
			return false;
	}

	private void saveDialogSettings( )
	{
		try
		{
			File file = new File( DIALOG_SETTING_FILE );
			if ( !file.exists( ) )
			{
				if ( !file.getParentFile( ).exists( ) )
					file.getParentFile( ).mkdirs( );
			}
			getDialogSettings( ).save( DIALOG_SETTING_FILE );
		}
		catch ( IOException e1 )
		{
			LogUtil.recordErrorMsg( e1, false );
		}
	}

	private void saveToolkitSettings( )
	{
		if ( data.getCurrentBRDProProject( ) != null )
		{
			getDialogSettings( ).put( CURRENT_BRDPRO_PROJECT,
					data.getCurrentBRDProProject( ) );
			IDialogSettings projectSetting = getDialogSettings( ).getSection( data.getCurrentBRDProProject( ) );
			if ( projectSetting == null )
			{
				projectSetting = getDialogSettings( ).addNewSection( data.getCurrentBRDProProject( ) );
			}

			projectSetting.put( DIRECTORY, data.getCurrentInstallBRDProData( )
					.getDirectory( ) );
			projectSetting.put( CLEARDIRECTORY,
					data.getCurrentInstallBRDProData( ).isNotClearDirectory( ) );
			projectSetting.put( CLOSEBRDPRO, data.getCurrentInstallBRDProData( )
					.isNotCloseBRDPro( ) );
			projectSetting.put( CREATESHORTCUT,
					data.getCurrentInstallBRDProData( ).isNotCreateShortcut( ) );
			projectSetting.put( SHORTCUTARGUMENTS,
					data.getCurrentInstallBRDProData( ).getShortcutArguments( ) );
			if ( data.getCurrentInstallBRDProData( ).getModules( ) != null )
			{
				StringBuffer buffer = new StringBuffer( );
				for ( int i = 0; i < data.getCurrentInstallBRDProData( )
						.getModules( ).length; i++ )
				{
					Module module = data.getCurrentInstallBRDProData( )
							.getModules( )[i];
					if ( module != null )
					{
						buffer.append( module.getName( ) );
						if ( i < data.getCurrentInstallBRDProData( )
								.getModules( ).length - 1 )
							buffer.append( ";" );
					}
				}
				projectSetting.put( MODULES, buffer.toString( ) );
			}
			else
			{
				projectSetting.put( MODULES, (String) null );
			}
		}

		if ( data.getCurrentIVProject( ) != null )
		{
			getDialogSettings( ).put( CURRENT_IV_PROJECT,
					data.getCurrentIVProject( ) );
			IDialogSettings projectSetting = getDialogSettings( ).getSection( data.getCurrentIVProject( ) );
			if ( projectSetting == null )
			{
				projectSetting = getDialogSettings( ).addNewSection( data.getCurrentIVProject( ) );
			}

			projectSetting.put( P4ROOT, data.getCurrentIportalViewerData( )
					.getRoot( ) );
			projectSetting.put( P4VIEW, data.getCurrentIportalViewerData( )
					.getView( ) );
			projectSetting.put( CUSTOMPROJECTNAME,
					data.getCurrentIportalViewerData( ).getCustomProjectName( ) );
			projectSetting.put( FORCEOPERATION,
					data.getCurrentIportalViewerData( ).isForceOperation( ) );
			projectSetting.put( REVERTFILES, data.getCurrentIportalViewerData( )
					.isRevertFiles( ) );
			projectSetting.put( P4SERVER, data.getCurrentIportalViewerData( )
					.getServer( ) );
			projectSetting.put( P4USER, data.getCurrentIportalViewerData( )
					.getUser( ) );
			projectSetting.put( P4PASSWORD, data.getCurrentIportalViewerData( )
					.getPassword( ) );
			projectSetting.put( P4CLIENT, data.getCurrentIportalViewerData( )
					.getClient( ) );
		}

		saveDialogSettings( );
	}
}
