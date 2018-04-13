package mic1.gaborFix;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

public class RememberPositionFrame extends Frame
{
	private static final String OLD_LOC_X = "old_loc_x";
	private static final String OLD_LOC_Y = "old_loc_y";
	private static final String OLD_WIDTH = "old_width";
	private static final String OLD_HEIGHT = "old_height";

	protected final Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

	public RememberPositionFrame(GraphicsConfiguration gc)
	{
		super(gc);
		init();
	}

	public RememberPositionFrame(String title) throws HeadlessException
	{
		super(title);
		init();
	}

	public RememberPositionFrame(String title, GraphicsConfiguration gc)
	{
		super(title, gc);
		init();
	}

	public RememberPositionFrame() throws HeadlessException
	{
		init();
	}

	private void init(){
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				setLocation(getLastLocation());
				setSize(getLastSize());
				super.windowOpened(e);
			}


			@Override
			public void windowClosed(WindowEvent e)
			{
				saveCurrentWindowState();
				super.windowClosed(e);
			}

			@Override
			public void windowActivated(WindowEvent e)
			{
				setLocation(getLastLocation());
				setSize(getLastSize());
				super.windowActivated(e);
			}

			@Override
			public void windowDeactivated(WindowEvent e)
			{
				saveCurrentWindowState();
				super.windowDeactivated(e);
			}

			@Override
			public void windowStateChanged(WindowEvent e)
			{
				saveCurrentWindowState();
				super.windowStateChanged(e);
			}

			@Override
			public void windowGainedFocus(WindowEvent e)
			{
				saveCurrentWindowState();
				super.windowGainedFocus(e);
			}

			@Override
			public void windowLostFocus(WindowEvent e)
			{
				saveCurrentWindowState();
				super.windowLostFocus(e);
			}
		});
	}



	protected Point getLastLocation(){
		Point currentLocation =  getLocation();
		Point point = new Point(
				prefs.getInt(OLD_LOC_X, currentLocation.x),
				prefs.getInt(OLD_LOC_Y, currentLocation.y)
		);
		//System.out.println("last_location: "+point);
		return point;
	}

	protected Dimension getLastSize(){
		Dimension currentSize =  getSize();
		Dimension size = new Dimension(
				prefs.getInt(OLD_WIDTH, currentSize.width),
				prefs.getInt(OLD_HEIGHT, currentSize.height)
		);
	//	System.out.println("last_size_access: "+size);
		return size;
	}

	protected void saveCurrentWindowState(){
		Point currentLocation =  getLocation();
		prefs.putInt(OLD_LOC_X,currentLocation.x);
		prefs.putInt(OLD_LOC_Y, currentLocation.y);
		Dimension currentSize = new Dimension(getWidth(), getHeight());
		prefs.putInt(OLD_WIDTH,currentSize.width);
		prefs.putInt(OLD_HEIGHT, currentSize.height);
	//	System.out.println("saved: "+currentLocation+" "+currentSize);
	}


}
