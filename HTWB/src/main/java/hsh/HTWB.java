package hsh;

import com.hp.hpl.jena.rdf.model.Resource;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import swt.SWTResourceManager;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;





public class HTWB {
	private DataBindingContext m_bindingContext;

	protected Shell shlHTWB;
	private Tree tree;
	private SKOSModel model = null;
	private Browser conceptBrowser;
	private Locale loc;
	Preferences prefs;
	private Map<Locale, Index> searchindex;

	private Label lblStatusbar;
	private Table searchresults;

	private static Text sourceViewer;

	private TableColumn resultcolumn;

	private Combo comboLangSel;
	
	private boolean modified;


	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				try {
					HTWB window = new HTWB();
					window.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	
	
	public HTWB() {
		prefs = Preferences.userNodeForPackage(getClass());	
		String language = prefs.get("language", "en");
		loc = new Locale(language);
		modified = false;
	}




	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlHTWB.open();
		shlHTWB.layout();
		while (!shlHTWB.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		
		shlHTWB = new Shell();
		shlHTWB.setImage(SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/SkosyIcon.png"));
		shlHTWB.setSize(850, 600);
		if(!LangRes.supports(loc)) {
			loc = new Locale("en");
			
		}
		shlHTWB.setText(LangRes.getStr(loc, "main.title"));
		RowLayout rl_shlHTWB = new RowLayout(SWT.VERTICAL);
		rl_shlHTWB.fill = true;
		shlHTWB.setLayout(rl_shlHTWB);
		
		Menu menu = new Menu(shlHTWB, SWT.BAR);
		shlHTWB.setMenuBar(menu);

		
		MenuItem mntmDatei_1 = new MenuItem(menu, SWT.CASCADE);
		mntmDatei_1.setText(LangRes.getStr(loc, "main.file"));
		
		Menu menu_1 = new Menu(mntmDatei_1);
		mntmDatei_1.setMenu(menu_1);
		
		MenuItem mntopen = new MenuItem(menu_1, SWT.NONE);
		mntopen.addSelectionListener(new DlgOpen(this));
		mntopen.setText(LangRes.getStr(loc, "main.open"));
		
		MenuItem mntsave = new MenuItem(menu_1, SWT.NONE);
		mntsave.addSelectionListener(new DlgSave(this));
		mntsave.setText(LangRes.getStr(loc, "main.save"));
		
		MenuItem mntclose = new MenuItem(menu_1, SWT.NONE);
		mntclose.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlHTWB.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mntclose.setText(LangRes.getStr(loc, "main.close"));
		
		MenuItem mntmEdit = new MenuItem(menu, SWT.CASCADE);
		mntmEdit.setText("Edit");
		
		Menu menu_3 = new Menu(mntmEdit);
		mntmEdit.setMenu(menu_3);
		
		MenuItem mntmInserNewSchema = new MenuItem(menu_3, SWT.NONE);
		mntmInserNewSchema.setText("Insert new schema");
		
		MenuItem mntmAddNewItem = new MenuItem(menu_3, SWT.NONE);
		mntmAddNewItem.setText("Add new concept");
		
		MenuItem mntmEditLabels = new MenuItem(menu_3, SWT.NONE);
		mntmEditLabels.setText("Edit labels");
		
		MenuItem mntmAdddeleteBroaderConcepts = new MenuItem(menu_3, SWT.NONE);
		mntmAdddeleteBroaderConcepts.setText("Add/delete related concepts");
		
		MenuItem mntmEditNotes = new MenuItem(menu_3, SWT.NONE);
		mntmEditNotes.setText("Edit notes");
		
		MenuItem mntmPreferences = new MenuItem(menu, SWT.NONE);
		mntmPreferences.setText(LangRes.getStr(loc, "main.prefs"));
		mntmPreferences.addSelectionListener(new DlgPreferences(this));
		
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText(LangRes.getStr(loc, "main.help"));
		
		Menu menu_2 = new Menu(mntmHelp);
		mntmHelp.setMenu(menu_2);
		
		MenuItem mntmAboutHtwb = new MenuItem(menu_2, SWT.NONE);
		mntmAboutHtwb.setText(LangRes.getStr(loc, "main.about"));
		mntmAboutHtwb.addSelectionListener(new DlgAbout(this));
		

		
		final Composite composite = new Composite(shlHTWB, SWT.NONE);
		RowLayout rowLayoutV = new RowLayout();
        rowLayoutV.wrap = false;
        rowLayoutV.pack = false;
        rowLayoutV.justify = true;
        rowLayoutV.type = SWT.VERTICAL;
        rowLayoutV.marginLeft = 0;
        rowLayoutV.marginTop = 5;
        rowLayoutV.marginRight = 0;
        rowLayoutV.marginBottom = 5;
        rowLayoutV.spacing = 5;
		composite.setLayout(rowLayoutV);

		
		
		final SashForm sashForm = new SashForm(composite, SWT.NONE);
		RowLayout rowLayoutH = new RowLayout();
        rowLayoutH.wrap = false;
        rowLayoutH.pack = false;
        rowLayoutH.justify = true;
        rowLayoutH.type = SWT.HORIZONTAL;
        rowLayoutH.marginLeft = 5;
        rowLayoutH.marginTop = 5;
        rowLayoutH.marginRight = 5;
        rowLayoutH.marginBottom = 5;
        rowLayoutH.spacing = 5;
        sashForm.setLayout(rowLayoutH);

		
		TabFolder tabFolderNav = new TabFolder(sashForm, SWT.NONE);
		TabItem tbtmHierarchy = new TabItem(tabFolderNav, SWT.NONE);
		tbtmHierarchy.setText(LangRes.getStr(loc, "main.hierar"));
		
		TabItem tbtmSearch = new TabItem(tabFolderNav, SWT.NONE);
		tbtmSearch.setText(LangRes.getStr(loc, "main.search"));
		
		Composite composite_1 = new Composite(tabFolderNav, SWT.NONE);
		tbtmSearch.setControl(composite_1);
		composite_1.setLayout(new FormLayout());	
		
		Button btnSearch = new Button(composite_1, SWT.PUSH);
		shlHTWB.setDefaultButton(btnSearch); // TODO Only in this form!
		
		FormData fd_btnSearch = new FormData();
		fd_btnSearch.right = new FormAttachment(100, -5);
		fd_btnSearch.bottom = new FormAttachment(0, 31);
		btnSearch.setLayoutData(fd_btnSearch);
		btnSearch.setText(LangRes.getStr(loc, "search.search"));
		
		
		
		final Text searchfield = new Text(composite_1, SWT.BORDER);
		FormData fd_text = new FormData();
		fd_text.bottom = new FormAttachment(0, 31);
		fd_text.right = new FormAttachment(btnSearch, -10);
		fd_text.top = new FormAttachment(0, 10);
		fd_text.left = new FormAttachment(0, 10);
		searchfield.setLayoutData(fd_text);
		
		Button btnExactSearch = new Button(composite_1, SWT.RADIO);
		FormData fd_btnExactSearch = new FormData();
		fd_btnExactSearch.top = new FormAttachment(searchfield, 6);
		fd_btnExactSearch.left = new FormAttachment(0, 10);
		btnExactSearch.setLayoutData(fd_btnExactSearch);
		btnExactSearch.setText(LangRes.getStr(loc, "search.exact"));
		btnExactSearch.setSelection(true);
		
		final Button btnFuzzySearch = new Button(composite_1, SWT.RADIO);
		FormData fd_btnFuzzySearch = new FormData();
		fd_btnFuzzySearch.top = new FormAttachment(btnExactSearch, 6);
		fd_btnFuzzySearch.left = new FormAttachment(searchfield, 0, SWT.LEFT);
		btnFuzzySearch.setLayoutData(fd_btnFuzzySearch);
		btnFuzzySearch.setText(LangRes.getStr(loc, "search.fuzzy"));
		
		
		Text txtS = new Text(composite_1, SWT.NONE | SWT.READ_ONLY);
		txtS.setEditable(false);
		txtS.setText(LangRes.getStr(loc, "search.srcLang"));
		FormData fd_txtS = new FormData();
		fd_txtS.right = new FormAttachment(btnExactSearch, 110, SWT.RIGHT);
		fd_txtS.bottom = new FormAttachment(btnExactSearch, 0, SWT.BOTTOM);
		fd_txtS.top = new FormAttachment(searchfield, 6);
		fd_txtS.left = new FormAttachment(btnExactSearch, 8);
		txtS.setLayoutData(fd_txtS);
		
		
		comboLangSel = new Combo(composite_1, SWT.NONE);
		FormData fd_combo = new FormData();
		fd_combo.right = new FormAttachment(txtS, 56, SWT.RIGHT);
		fd_combo.left = new FormAttachment(txtS, 6);
		fd_combo.top = new FormAttachment(searchfield, 6);
		comboLangSel.setLayoutData(fd_combo);
		
		
		searchresults = new Table(composite_1, SWT.BORDER | SWT.FULL_SELECTION);
		FormData fd_table = new FormData();
		fd_table.top = new FormAttachment(btnFuzzySearch, 6);
		fd_table.bottom = new FormAttachment(100, -5);
		fd_table.left = new FormAttachment(0,5);
		fd_table.right = new FormAttachment(100, -5);
		searchresults.setLayoutData(fd_table);
	    resultcolumn = new TableColumn(searchresults, SWT.LEFT);
	    TableColumn uricolumn = new TableColumn(searchresults, SWT.LEFT);
	    
	    resultcolumn.setWidth(400);
	    uricolumn.setWidth(0);
	    

		
			
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String searchtext = searchfield.getText();
				
				Font basefont = searchresults.getFont();
				FontData[] itfd = basefont.getFontData();
				FontData[] bfd = basefont.getFontData();
				for(int i = 0; i < itfd.length; i++) {
					itfd[i].setStyle(SWT.ITALIC);
				}
				for(int i = 0; i < bfd.length; i++) {
					bfd[i].setStyle(SWT.BOLD);
				}
				
				Font fontitalic = new Font(basefont.getDevice(),itfd);
				Font fontbold = new Font(basefont.getDevice(),bfd);
				
				searchresults.clearAll();
				searchresults.setItemCount(0);
				if(searchindex == null || searchindex.isEmpty()) {
					return;
				}

				int nLangSel = comboLangSel.getSelectionIndex();
				if(nLangSel < 0) nLangSel = 0; //0 is any Language, -1 is nothing selected   
				String selectedLang = comboLangSel.getItem(nLangSel);
				for(Entry<Locale,Index> entry:searchindex.entrySet()) {
					Index ind = entry.getValue();
					Locale resloc = entry.getKey();
					if(nLangSel == 0 || resloc.getLanguage().equalsIgnoreCase(selectedLang)) {
						ArrayList<SearchResult> results = ind.find(searchtext,btnFuzzySearch.getSelection());
						if(results != null) {
							for(SearchResult sr:results) {
								ConceptInfo c = sr.getConcept();
								TableItem it = new TableItem(searchresults, SWT.NONE);
								String uri = c.getUri();
								String label = c.getLabel();
								String term = c.getTerm();

								it.setText(new String[] {term, uri});

								if("prefLabel".equals(label)) {
									it.setFont(fontbold);
								} else if("altLabel".equals(label)) {
									it.setFont(fontitalic);
								}
								it.setImage(0, SWTResourceManager.getImage(HTWB.class,"/de/hsh/icon/"+ resloc.getLanguage() +".png"));
							}
						}
					}
				}
			}
		});
			
		tree = new Tree(tabFolderNav, SWT.NONE | SWT.VIRTUAL);
		tbtmHierarchy.setControl(tree);
		
		TabFolder tabFolderView = new TabFolder(sashForm, SWT.NONE);	
		
		TabItem tbtmConceptview = new TabItem(tabFolderView, SWT.NONE);
		tbtmConceptview.setText(LangRes.getStr(loc, "main.concept"));
		
		conceptBrowser = new Browser(tabFolderView, SWT.NONE);
		tbtmConceptview.setControl(conceptBrowser);
		
		TabItem tbtmSourceView = new TabItem(tabFolderView, SWT.NONE);
		tbtmSourceView.setText(LangRes.getStr(loc, "main.source"));
		
		
		sourceViewer = new Text(tabFolderView, SWT.READ_ONLY | SWT.WRAP |  SWT.V_SCROLL); 
		tbtmSourceView.setControl(sourceViewer);
		sashForm.setWeights(new int[] {282, 482});
		
		lblStatusbar = new Label(shlHTWB, SWT.BORDER);
		lblStatusbar.setText("");
		lblStatusbar.setLayoutData(new RowData(composite.getClientArea().width, 15));
		
		
		shlHTWB.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				
				composite.setBounds(new Rectangle(shlHTWB.getClientArea().x,shlHTWB.getClientArea().y,shlHTWB.getClientArea().width-5,shlHTWB.getClientArea().height-30));

				composite.setLayoutData(new RowData(shlHTWB.getClientArea().width-5, shlHTWB.getClientArea().height-30));
				sashForm.setBounds(composite.getClientArea());
				sashForm.setLayoutData(new RowData(composite.getClientArea().width, composite.getClientArea().height-5));		

				int w = (int) (100.0 + 100.0/(1.0+Math.exp(shlHTWB.getClientArea().width/-300d)));
				sashForm.setWeights(new int[] {100, w});  
				sashForm.redraw();		
			}
		});
		
		
		searchresults.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {	
				resultcolumn.setWidth(searchresults.getClientArea().width);	
			}
		});
		
		conceptBrowser.addLocationListener(new LocationAdapter() {
			 public void changing(LocationEvent event) {
			        try {

			        	
			            // Setting event.doit to false prevents the link from opening in place
			        	if("about:blank".equals(event.location)) {
			        		event.doit = true;
			        	} else {
			        		//System.out.println(event.location);
			        		model.selectURI(event.location,conceptBrowser,sourceViewer);
			        		event.doit = false;
			        	}
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			    }
		});
		
		searchresults.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TableItem ti = (TableItem) e.item;
				String uri = ti.getText(1);
				if(uri != null) {
					model.selectURI(uri,conceptBrowser,sourceViewer);
				}
			}  
		});
		
		m_bindingContext = initDataBindings();

	}
	
	
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		return bindingContext;
	}
	
	public void setThesaurus(String path) {
		if(model != null) {
			tree.clearAll(true);
			tree.setItemCount(0);
			comboLangSel.removeAll();
			conceptBrowser.setText("");
			sourceViewer.selectAll();
			sourceViewer.clearSelection();
		}
		model = new SKOSModel(path,lblStatusbar);
		lblStatusbar.setText(LangRes.getStr(loc, "status.buildtree"));
		model.Display(tree);
		lblStatusbar.setText(LangRes.getStr(loc, "status.buildindex"));
		searchindex = model.buildIndex();
		comboLangSel.add(LangRes.getStr(loc, "search.any"));
		for(Locale loc:searchindex.keySet()) {			
			comboLangSel.add(loc.getLanguage());
		}
		lblStatusbar.setText("");
		
		
		
		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem ti = (TreeItem) e.item;
				Resource c = (Resource) ti.getData();
				if(c != null) {
					model.displayConcept(c, conceptBrowser, sourceViewer);
				} else if(ti == tree.getTopItem()) {
					model.displayFile(conceptBrowser);
				}
			}  
		});
	}


	public void updatePrefs() {
		try {
			prefs.sync();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String language = prefs.get("language", "en");
		loc = new Locale(language);
		//shlHTWB.redraw();
	}




	public boolean isModified() {
		// TODO Auto-generated method stub
		return modified;
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
	}
}
