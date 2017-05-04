package de.hsh;

import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class DlgPreferences implements SelectionListener {

	
	private HTWB parent;
	protected Shell shlPref;
	private Locale loc;
	private Display display;
	Preferences prefs;
	
	public DlgPreferences(HTWB htwb) {
		super();	
		parent = htwb;
		prefs = Preferences.userNodeForPackage(parent.getClass());	
		String language = prefs.get("language", "en");
		loc = new Locale(language);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {

	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		open();
	}
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlPref.open();
		shlPref.layout();
		while (!shlPref.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createContents() {
		shlPref = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shlPref.setImage(SWTResourceManager.getImage(DlgPreferences.class, "/de/hsh/icon/SkosyIcon.png"));
		shlPref.setSize(450, 289);
		
		TabFolder tabFolder = new TabFolder(shlPref, SWT.NONE);
		tabFolder.setBounds(0, 0, 444, 226);
		
		TabItem tbtmAllgemein = new TabItem(tabFolder, SWT.NONE);
		tbtmAllgemein.setText("Allgemein");
		
		Canvas canvas = new Canvas(tabFolder, SWT.NONE);
		tbtmAllgemein.setControl(canvas);
		
		Group grpSprache = new Group(canvas, SWT.NONE);
		grpSprache.setText("Sprache");
		grpSprache.setBounds(10, 10, 392, 56);
		
		final Combo combo = new Combo(grpSprache, SWT.NONE);
		combo.setBounds(10, 23, 138, 23);
		combo.add("Deutsch",0);
		combo.add("Englisch",1);
		combo.add("Niederländisch",2);
		
		int selectedLang = 0;
		switch(loc.getLanguage()) {
		case "de":
			selectedLang = 0;
			break;
		case "en":
			selectedLang = 1;
			break;
		case "nl":
			selectedLang = 2;
			break;
		}
		
		combo.select(selectedLang);
		
		combo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				switch(combo.getSelectionIndex()) {
				case 0:
					loc = new Locale("de");
					prefs.put("language", "de");
					break;
				case 1:
					loc = new Locale("en");
					prefs.put("language", "en");
					break;
				case 2:
					loc = new Locale("nl");
					prefs.put("language", "nl");
					break;
				
				}
				
				//System.out.println("Selected index: " + combo.getSelectionIndex() + ", selected item: " + combo.getItem(combo.getSelectionIndex()) + ", text content in the text field: " + combo.getText());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		

		Button btnOk = new Button(shlPref, SWT.NONE);
		btnOk.setBounds(288, 232, 75, 25);
		btnOk.setText("OK");
		btnOk.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					prefs.flush();
					parent.updatePrefs();
				} catch (BackingStoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				shlPref.close();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Button btnAbbrechen = new Button(shlPref, SWT.NONE);
		btnAbbrechen.setBounds(369, 232, 75, 25);
		btnAbbrechen.setText("Abbrechen");
		btnAbbrechen.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				shlPref.close();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	}
}
