package hsh.gui;

import java.util.Locale;

import hsh.HTWB;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import swt.SWTResourceManager;
/**
 * SWT based dialog that informs about this software
 *
 * @author  Christian Wartena
 */
public class DlgAbout implements SelectionListener {

	private HTWB parent;
	protected Shell shlAbout;
	private Locale loc;
	private Display display;
	private Text txtHannoverThesaurusWorkbench;
	
	
	public DlgAbout(HTWB htwb) {
		super();	
		parent = htwb;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		open();

	}
	
	/**
	 * //wbp.parser.entryPoint
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlAbout.open();
		shlAbout.layout();
		while (!shlAbout.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createContents() {
		shlAbout = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shlAbout.setImage(SWTResourceManager.getImage(DlgAbout.class, "/de/hsh/icon/SkosyIcon.png"));
		shlAbout.setSize(450, 217);
		
		Canvas canvas = new Canvas(shlAbout, SWT.NONE);
		canvas.setBounds(0, 0, 98, 42);
		
		drawLogoToCanvas(canvas);

		
		Button btnOk = new Button(shlAbout, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlAbout.close();
			}
		});
		btnOk.setBounds(14, 154, 75, 25);
		btnOk.setText("OK");
		
		txtHannoverThesaurusWorkbench = new Text(shlAbout, SWT.BORDER|SWT.WRAP);
		txtHannoverThesaurusWorkbench.setEditable(false);
		txtHannoverThesaurusWorkbench.setText("Skosy v0.0\r\nEasy Skos Editing\r\nWritten by Hochschule Hannover - Christian Wartena\r\nThis is a an unstable developer preview\r\n2015");
		txtHannoverThesaurusWorkbench.setBounds(97, 0, 337, 179);
		
	}

	private void drawLogoToCanvas(Canvas canvas) {
		Image logo = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/SkosyLogoSmall.jpg");
		ImageData imgData = logo.getImageData();
		

		int width = canvas.getBounds().width;
		int height = canvas.getBounds().height;

		//Image scaledImage = new Image(display, imgData.scaledTo(width,height));
		//scaledImage.setBackground(new Color(display, 100,100,100));
		canvas.setBackgroundImage(logo); //scaledImage);
		
	}
		
	
}
