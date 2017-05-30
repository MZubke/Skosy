package hsh.skosy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import swt.SWTResourceManager;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;


public class SKOSModel {

	private Locale loc;
	Preferences prefs;
	private String filePath;
	private InfModel ontology = null; 
	String thesName = "";
	private Property preflabel = null;
	private Property altLabel = null;
	private Property xpreflabel = null;
	private Property xaltLabel = null;
	private Property xlitform = null;
	private Property notation = null;
	private Property inscheme = null;
	private Property broader = null;
	private Property narrower = null;
	private Property related = null;
	private Property topconceptof = null;
	private Property hastopconcept = null;
	private Property note = null;
	private Property rdfslabel = null;
	private Property rdftype = null;
	private Property rdfvalue = null;
	private Resource skos_concept = null;
	private Resource skos_schema = null;
	private Property owl_deprecated = null;
	private final static String skos = "http://www.w3.org/2004/02/skos/core#";
	private final static String skosxl = "http://www.w3.org/2008/05/skos-xl#";
	private final static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	private final static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final static String dcterms = "http://purl.org/dc/terms/";
	private final static String dc = "http://purl.org/dc/elements/1.1/";
	private final String imagepath;
	private Map<String,TreeItem> uri2tree;
	private Map<Resource,String> concept2notation; //caching
	private Tree hierarchy;
	private HashSet<Property> standardProperties = null;


	public SKOSModel(String path, Label lblStatusbar) {
		prefs = Preferences.userNodeForPackage(getClass());	
		String language = prefs.get("language", "en");
		loc = new Locale(language);
		
		filePath = path;
		String ext = filePath.substring(filePath.lastIndexOf(".")+1);
		String fn = filePath.substring(Math.max(filePath.lastIndexOf("\\"),filePath.lastIndexOf("/"))+1);
		lblStatusbar.setText(LangRes.getStr(loc, "status.reading") + " "+ fn + " ...");
		
		
		
		InfModel skosdef = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
		ontology = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);

		try {		
			if("rdf".equalsIgnoreCase(ext) || "rdfs".equalsIgnoreCase(ext) ||"xml".equalsIgnoreCase(ext) ) {
				RDFReader rdfreader = ontology.getReader("RDF/XML");
				//For faster reading according to https://jena.apache.org/documentation/io/rdfxml_howto.html#faster-rdfxml-io
				rdfreader.setProperty("WARN_REDEFINITION_OF_ID","EM_IGNORE"); 
				//String encoding = readEncodingFromXML(filePath);
				//InputStreamReader reader = new  InputStreamReader(new FileInputStream(filePath),encoding);
				//ontology.read(reader, null,"RDF/XML");
				ontology.read(new FileInputStream(filePath), null,"RDF/XML");
			} else if("ttl".equalsIgnoreCase(ext)) {
				ontology.read(new FileInputStream(filePath), null,"TURTLE");
			} else {
				throw(new FileNotFoundException(LangRes.getStr(loc, "error.unknownext") + " " + ext));			
			}
	
			InputStream skosstream = getClass().getResourceAsStream("/de/hsh/rdf/skos.rdf");
			skosdef.read(skosstream, null,"RDF/XML");	
			ontology.add(skosdef);
			
			findProperties();
			lblStatusbar.setText(LangRes.getStr(loc, "status.addstatements"));
			normalizeSkos();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		final File f = new File(SKOSModel.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		//imagepath = f.getAbsolutePath() + "/de/hsh/icon/";
		String exepath = f.getAbsolutePath(); //+"/../img/";
		int lastslash = exepath.lastIndexOf('/') > 0 ? exepath.lastIndexOf('/') : exepath.lastIndexOf('\\');
		imagepath = exepath.substring(0, lastslash) +"/img/";
		//imagepath = getClass().getResource("/de/hsh/icon/" ).toString();

		
		
		uri2tree = new HashMap<String,TreeItem>();
		concept2notation = new HashMap<Resource,String>();
		standardProperties = new HashSet<Property>();
		standardProperties.add(altLabel);
		standardProperties.add(preflabel);
		standardProperties.add(xaltLabel);
		standardProperties.add(xpreflabel);
		standardProperties.add(topconceptof);
		standardProperties.add(hastopconcept);
		standardProperties.add(broader);
		standardProperties.add(narrower);
		standardProperties.add(related);
		standardProperties.add(inscheme);
		standardProperties.add(notation);
		standardProperties.add(note);
		standardProperties.add(rdftype);
	}

	private void normalizeSkos() {
		
		ArrayList<Statement> newStatements	= 	new ArrayList<Statement>();
				
		ResIterator conceptit = ontology.listSubjectsWithProperty(rdftype, skos_concept);
		while(conceptit.hasNext()) {
			Resource concept = conceptit.nextResource();
		
			StmtIterator brdit = concept.listProperties(broader);
			while(brdit.hasNext())
			{
				Resource brdconcept = (Resource) brdit.nextStatement().getObject().as(Resource.class);
				Statement st = ontology.createStatement(brdconcept,narrower,(RDFNode) concept.as(RDFNode.class));
				if(!ontology.contains(st)) {
					newStatements.add(st);
				}
			}
			StmtIterator nrwit = concept.listProperties(narrower);
			while(nrwit.hasNext())
			{
				Resource nrwconcept = (Resource) nrwit.nextStatement().getObject().as(Resource.class);
				Statement st = ontology.createStatement(nrwconcept,broader,(RDFNode) concept.as(RDFNode.class));
				if(!ontology.contains(st)) {
					newStatements.add(st);
				}
			}
			
			StmtIterator topit = concept.listProperties(topconceptof);
			while(topit.hasNext())
			{
				Resource scheme = (Resource) topit.nextStatement().getObject().as(Resource.class);
				Statement st = ontology.createStatement(scheme,hastopconcept,(RDFNode) concept.as(RDFNode.class));
				if(!ontology.contains(st)) {					
					newStatements.add(st);
				}
			}		
		}
		
		Statement[] nStArray = new Statement[newStatements.size()];
		nStArray = newStatements.toArray(nStArray);
		ontology.add(nStArray);
		newStatements.clear();

		ResIterator schemeit =  ontology.listSubjectsWithProperty(topconceptof);
		while(schemeit.hasNext()) {
			Resource scheme = (Resource) schemeit.nextResource();
			StmtIterator topit = scheme.listProperties(topconceptof);
			while(topit.hasNext())
			{
				Resource concept = (Resource) topit.nextStatement().getObject().as(Resource.class);
				Statement st = ontology.createStatement(concept,topconceptof,(RDFNode) concept.as(RDFNode.class));
				if(!ontology.contains(st)) {
					newStatements.add(st);
				}
			}
		}
		
		// Concepts without broader concepts within a scheme should become topconcepts of that scheme 
		conceptit = ontology.listSubjectsWithProperty(rdftype, skos_concept);
		while(conceptit.hasNext()) {
			Resource concept = conceptit.nextResource();
			if(concept.getProperty(broader) == null) {
				//System.out.println(concept.getLocalName());
				StmtIterator schemait  = concept.listProperties(inscheme);
				while(schemait.hasNext()) {
					Statement inSchemaStmt = schemait.nextStatement();
					RDFNode schema =  inSchemaStmt.getObject();
					//System.out.println("Schema: " + schema.toString());
					Statement st = ontology.createStatement(concept,topconceptof,schema);
					if(!ontology.contains(st)) {
						newStatements.add(st);
					}
					st = ontology.createStatement((Resource) schema.as(Resource.class),hastopconcept,concept);
					if(!ontology.contains(st)) {
						newStatements.add(st);					}
				}
				
			}
		}
		
		nStArray = new Statement[newStatements.size()];
		nStArray = newStatements.toArray(nStArray);
		ontology.add(newStatements);
		
	}

	private void findProperties() {
		preflabel = ontology.getProperty(skos+"prefLabel");
		if(preflabel == null) {
			preflabel = ontology.createProperty(skos+"prefLabel");
		}
		altLabel = ontology.getProperty(skos+"altLabel");
		if(altLabel == null) {
			altLabel = ontology.createProperty(skos+"altLabel");
		}
		notation = ontology.getProperty(skos+"notation");
		if(notation == null) {
			notation = ontology.createProperty(skos+"notation");
		}
		inscheme = ontology.getProperty(skos+"inScheme");
		if(inscheme == null) {
			inscheme = ontology.createProperty(skos+"inScheme");
		}
		broader = ontology.getProperty(skos+"broader");
		narrower = ontology.getProperty(skos+"narrower");
		related = ontology.getProperty(skos+"related");
		topconceptof = ontology.getProperty(skos+"topConceptOf");
		hastopconcept = ontology.getProperty(skos+"hasTopConcept");
		note = ontology.getProperty(skos+"note");
		xpreflabel = ontology.getProperty(skosxl+"prefLabel");
		xaltLabel = ontology.getProperty(skosxl+"altLabel");
		xlitform = ontology.getProperty(skosxl+"literalForm");
		rdfvalue = ontology.getProperty(rdf+"value");
		owl_deprecated = ontology.getProperty("http://www.w3.org/2002/07/owl#deprecated");
		
		rdftype = ontology.createProperty(rdf+"type");
		rdfslabel = ontology.createProperty(rdfs+"label");
		skos_concept = ontology.createResource(skos + "Concept");
		skos_schema = ontology.createResource(skos + "ConceptScheme");
		
		
	}

	public void Display(Tree tree) {
		
		hierarchy = tree;
		TreeItem root = new TreeItem(tree,SWT.NONE);
		root.setText(filePath.substring(filePath.lastIndexOf("\\")+1));
		Image icon_file = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/file.png");
		Image icon_schema = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/schema.png");
		Image icon_concept = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/concept.png");

		root.setImage(icon_file);
	
		ArrayList<Resource> schemata = getTopSchemata(null);
		for(Resource schema:schemata) {
			//String x = thesNode.getPropertyValue(preflabel).toString();
			TreeItem t_schema = new TreeItem(root,SWT.NONE);
			t_schema.setText(notationorlabel(schema));
			t_schema.setImage(icon_schema);
			t_schema.setData(schema);
			uri2tree.put(schema.getURI(), t_schema);
			DisplaySchema(schema,t_schema);
		}
		ArrayList<Resource> concepts = getTopConcepts(null);
		for(Resource concept:concepts) {
			//String x = thesNode.getPropertyValue(preflabel).toString();
			TreeItem t_concept = new TreeItem(root,SWT.NONE);
			t_concept.setText(notationorlabel(concept));
			t_concept.setImage(icon_concept);
			t_concept.setData(concept);
			uri2tree.put(concept.getURI(), t_concept);
			DisplayConcept(concept,t_concept);
		}

		
		root.setExpanded(true);

	}

	private void DisplaySchema(Resource parentschema, TreeItem root) {
		Image icon_schema = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/schema.png");
		Image icon_concept = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/concept.png");
		Image icon_conceptd = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/concept_d.png");
		ArrayList<Resource> schemata = getTopSchemata(parentschema);
		for(Resource schema:schemata) {
			if(schema.equals(parentschema)) continue;
			//String x = thesNode.getPropertyValue(preflabel).toString();
			TreeItem t_schema = new TreeItem(root,SWT.NONE);
			t_schema.setText(notationorlabel(schema));		
			t_schema.setImage(icon_schema);
			t_schema.setData(schema);
			uri2tree.put(schema.getURI(), t_schema);
			DisplaySchema(schema,t_schema);
		}
		ArrayList<Resource> concepts = getTopConcepts(parentschema);
		for(Resource concept:concepts) {
			TreeItem t_concept = new TreeItem(root,SWT.NONE);
			t_concept.setText(notationorlabel(concept));
			if(concept.hasLiteral(owl_deprecated, true)) {
				t_concept.setImage(icon_conceptd);
			} else {
				t_concept.setImage(icon_concept);
			}
			t_concept.setData(concept);
			uri2tree.put(concept.getURI(), t_concept);
			DisplayConcept(concept,t_concept);
		}
		
	}

	private void DisplayConcept(Resource parentconcept, TreeItem root) {
		Image icon_concept = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/concept.png");
		Image icon_conceptd = SWTResourceManager.getImage(HTWB.class, "/de/hsh/icon/concept_d.png");
		ArrayList<Resource> concepts = getNarrowerConcepts(parentconcept);
		for(Resource concept:concepts) {
			TreeItem t_concept = new TreeItem(root,SWT.NONE);

			t_concept.setData(concept);
			uri2tree.put(concept.getURI(), t_concept);
			t_concept.setText(notationorlabel(concept));
			if(concept.hasLiteral(owl_deprecated, true)) {
				t_concept.setImage(icon_conceptd);
			} else {
				t_concept.setImage(icon_concept);
			}

			DisplayConcept(concept,t_concept);
		}
		
	}

	private ArrayList<Resource> getTopSchemata(Resource parentSchema) {
		ArrayList<Resource> schemata = new ArrayList<Resource>();
		
		
		ResIterator iterator = ontology.listSubjectsWithProperty(rdftype, skos_schema);
		while(iterator.hasNext()) {
			Resource ind = (Resource) iterator.next();
			if((ind.getProperty(inscheme) == null && parentSchema == null )
					|| ind.hasProperty(inscheme,parentSchema)) {
				schemata.add(ind);
			}
		}
		
		
		/* Following works if ontology is a OntModel
		OntClass SchemaCls = ontology.getOntClass(skos + "ConceptScheme");
		if(SchemaCls != null) {
			ExtendedIterator iterator = SchemaCls.listInstances();
			while(iterator.hasNext()) {
				Individual ind = (Individual) iterator.next();
				if((ind.getPropertyValue(inscheme) == null && parentSchema == null )
						|| ind.hasProperty(inscheme,parentSchema)) {
					schemata.add(ind);
				}
			}
		}*/
		
		
		
		return schemata;
	}
	
	private ArrayList<Resource> getTopConcepts(Resource parentSchema) {
		ArrayList<Resource> concepts = new ArrayList<Resource>();
		if(parentSchema != null) {
			StmtIterator iterator = parentSchema.listProperties(hastopconcept);
			while(iterator.hasNext()) {
				RDFNode concept = iterator.nextStatement().getObject();
				concepts.add((Resource) concept.as(Resource.class));
			}
		} else {
			ResIterator iterator = ontology.listSubjectsWithProperty(rdftype, skos_concept);
			while(iterator.hasNext()) {
				Resource concept = iterator.nextResource();
				if(concept.getProperty(broader) == null  
					&&	concept.getProperty(inscheme) == null
					&&	concept.getProperty(topconceptof) == null) {
					concepts.add((Resource) concept.as(Resource.class));
				}
			}
		}

		Collections.sort(concepts, new Comparator<Resource>() {
			@Override
			public int compare(Resource  r1, Resource  r2)
			{
				return  notationorlabel(r1).compareTo(notationorlabel(r2));
			}
		});

		return concepts;
	}

	
	private ArrayList<Resource> getNarrowerConcepts(Resource parentConcept) {
		ArrayList<Resource> concepts = new ArrayList<Resource>();
		//NodeIterator iterator = parentConcept.listPropertyValues(narrower);
		StmtIterator iterator = parentConcept.listProperties(narrower);
		while(iterator.hasNext()) {
			RDFNode ind = iterator.nextStatement().getObject();   //iterator.nextNode();
			concepts.add((Resource) ind.as(Resource.class));
		}
		
	
		Collections.sort(concepts, new Comparator<Resource>() {
	        @Override
	        public int compare(Resource  r1, Resource  r2)
	        {
	            return  notationorlabel(r1).compareTo(notationorlabel(r2));
	        }
	    });
	    
	    
		
		/*
		OntClass ConceptCls = ontology.getOntClass(skos + "Concept");
		if(ConceptCls != null) {
			ExtendedIterator iterator = ConceptCls.listInstances();
			while(iterator.hasNext()) {
				Individual ind = (Individual) iterator.next();
				if(ind.hasProperty(broader,parentConcept) || parentConcept.hasProperty(narrower,ind))  {
					concepts.add(ind);
				}
			}
		}
		*/
		
		return concepts;
	}

	
	private ArrayList<Resource> getBroaderConcepts(Resource parentConcept) {
		ArrayList<Resource> concepts = new ArrayList<Resource>();
		//NodeIterator iterator = parentConcept.listPropertyValues(narrower);
		StmtIterator iterator = parentConcept.listProperties(broader);
		while(iterator.hasNext()) {
			RDFNode ind = iterator.nextStatement().getObject();   //iterator.nextNode();
			concepts.add((Resource) ind.as(Resource.class));
		}
		
		Collections.sort(concepts, new Comparator<Resource>() {
	        @Override
	        public int compare(Resource  r1, Resource  r2)
	        {
	            return  notationorlabel(r1).compareTo(notationorlabel(r2));
	        }
	    });
			
		return concepts;
	}
	
	
	private ArrayList<Resource> getRelatedConcepts(Resource parentConcept) {
		ArrayList<Resource> concepts = new ArrayList<Resource>();
		//NodeIterator iterator = parentConcept.listPropertyValues(narrower);
		StmtIterator iterator = parentConcept.listProperties(related);
		while(iterator.hasNext()) {
			RDFNode ind = iterator.nextStatement().getObject();   //iterator.nextNode();
			if(ind.isResource()) {
				concepts.add((Resource) ind.as(Resource.class));
			} else {
				System.out.println("Related:" + ind.toString());
			}
		}
		
		Collections.sort(concepts, new Comparator<Resource>() {
	        @Override
	        public int compare(Resource  r1, Resource  r2)
	        {
	            return  notationorlabel(r1).compareTo(notationorlabel(r2));
	        }
	    });
			
		return concepts;
	}

	
	private ArrayList<Resource> getTopConceptsSchemes(Resource parentConcept) {
		ArrayList<Resource> concepts = new ArrayList<Resource>();
		StmtIterator iterator = parentConcept.listProperties(topconceptof);
		while(iterator.hasNext()) {
			RDFNode ind = iterator.nextStatement().getObject();   
			concepts.add((Resource) ind.as(Resource.class));
		}
		
		Collections.sort(concepts, new Comparator<Resource>() {
	        @Override
	        public int compare(Resource  r1, Resource  r2)
	        {
	            return  notationorlabel(r1).compareTo(notationorlabel(r2));
	        }
	    });
			
		return concepts;
	}
	
	
	
	private String notationorlabel(Resource indiv) {
		//TODO Method is quite time consuming. Therefore we cache the results.
		
		String sNotation = concept2notation.get(indiv);
		if(sNotation != null && !sNotation.isEmpty()) {
			return sNotation;
		}
			
		Literal label = null;
		//1. Try to get a notation
		Statement notstmt = indiv.getProperty(notation); //TODO There might be more than one notation
		if(notstmt != null) {
			sNotation = ((Literal) notstmt.getObject().as(Literal.class)).getString();
		}
		//2. Try to get a skos-label in the preferred Language
		if(sNotation == null || sNotation.isEmpty())	{
			label = null;
			StmtIterator iter = indiv.listProperties(preflabel);
			while(iter.hasNext()) {
				RDFNode n = iter.nextStatement().getObject();
				label = (Literal) n.as(Literal.class);		
				if(label.getLanguage().equalsIgnoreCase(loc.getLanguage())) {
					sNotation = label.getString();
					break;
				} else if("en".equalsIgnoreCase(label.getLanguage())) {
					sNotation = label.getString();
				} else if(sNotation == null || sNotation.isEmpty()) {
					sNotation = label.getString();
				} 
			}
		}
		//3 Try to get a rdfs-label in the preferred Language
		if(sNotation == null || sNotation.isEmpty())	{
			label = null;
			StmtIterator iter = indiv.listProperties(rdfslabel);
			while(iter.hasNext()) {
				RDFNode n = iter.nextStatement().getObject();
				label = (Literal) n.as(Literal.class);		
				if(label.getLanguage().equalsIgnoreCase(loc.getLanguage())) {
					sNotation = label.getString();
					break;
				} else if("en".equalsIgnoreCase(label.getLanguage())) {
					sNotation = label.getString();
				} else if(sNotation == null || sNotation.isEmpty()) {
					sNotation = label.getString();
				} 
			}
		}
		

		if(sNotation == null || sNotation.isEmpty())	{
			sNotation = indiv.getLocalName();
		}
		if(sNotation == null || sNotation.isEmpty())	{
			sNotation = indiv.getURI();
		}
	
		concept2notation.put(indiv, sNotation);
			
		return sNotation;
	}
	
	private String getNotation(Resource indiv) {
		String sNotation = null;
	
		Statement notstmt = indiv.getProperty(notation); //TODO There might be more than one notation
		if(notstmt != null) {
			sNotation = ((Literal) notstmt.getObject().as(Literal.class)).getString();
		}
		
		return sNotation;
	}

	private String getNotes(Resource indiv) {
		String strNotes = "";
	
		StmtIterator stmtit = indiv.listProperties(note);
		while(stmtit.hasNext()) {
			Statement noteStmt = stmtit.nextStatement();
			//TODO Note can be something else than a literal!
			RDFNode object = noteStmt.getObject();
			strNotes += getLiteralHTML(object);
			/*
			if(note != null){
				if("en".equals(note.getLanguage())) {
					strNotes = note.getString();
				} else if(loc.getLanguage().equals(note.getLanguage())) {
					strNotes = note.getString();
					break;
				} else if(strNotes.isEmpty()) {
					strNotes = note.getString();
				}
			} 
			*/
			
			if(stmtit.hasNext()) {
				strNotes += "<br/>";
			}
		}
		

		
		return strNotes;
	}
	
	private Map<String, String> getPrefLabels(Resource concept) {
		Map<String,String> prefLabs = new HashMap<String,String>();	
		
		Literal label = null;
		StmtIterator iter = concept.listProperties(preflabel);
		while(iter.hasNext()) {
			RDFNode n = iter.nextStatement().getObject();
			label = (Literal) n.as(Literal.class);		
			String lang = label.getLanguage();
			if(lang == null || lang.isEmpty()) lang = "en";
			prefLabs.put(lang, label.getString());
		}
		
		iter = concept.listProperties(xpreflabel);
		while(iter.hasNext()) {
			Resource xlabel = (Resource) iter.nextStatement().getObject().as(Resource.class);
			Statement litform = xlabel.getProperty(xlitform);
			if(litform == null) {
				continue;
			}
			label = (Literal) litform.getObject().as(Literal.class);
			String lang = label.getLanguage();
			if(lang == null || lang.isEmpty()) lang = "en";
			prefLabs.put(lang, label.getString());
		}
		
		return prefLabs;
	}
	
	
	private HashMap<String, SortedSet<String>> getAltLabels(Resource concept) {
		
		HashMap<String, SortedSet<String>> altLabs = new HashMap<String,SortedSet<String>>();
		Literal label = null;
		StmtIterator iter = concept.listProperties(altLabel);
		while(iter.hasNext()) {
			RDFNode n = iter.nextStatement().getObject();
			label = (Literal) n.as(Literal.class);		
			String lang = label.getLanguage();
			if(lang == null || lang.isEmpty()) lang = "en";
			SortedSet<String> sortedlabels = altLabs.get(lang);
			if(sortedlabels == null) {
				sortedlabels = new TreeSet<String>();
			}
			sortedlabels.add(label.getString());
			altLabs.put(lang, sortedlabels);
		}

		iter = concept.listProperties(xaltLabel);
		while(iter.hasNext()) {
			Resource xlabel = (Resource) iter.nextStatement().getObject().as(Resource.class);
			Statement litform = xlabel.getProperty(xlitform);
			if(litform == null) {
				continue;
			}
			label = (Literal) litform.getObject().as(Literal.class);
			String lang = label.getLanguage();
			if(lang == null || lang.isEmpty()) lang = "en";
			SortedSet<String> sortedlabels = altLabs.get(lang);
			if(sortedlabels == null) {
				sortedlabels = new TreeSet<String>();
			}
			sortedlabels.add(label.getString());
			altLabs.put(lang, sortedlabels);
		}
		
		return altLabs;
	}
	
	private String getLiteralHTML(RDFNode node) {
		Literal lit = null;
		String sLit = "";

		if(node.isLiteral()) {
			lit = (Literal) node.as(Literal.class);
			sLit = lit.getString();
		} else if(node.isResource()) {
			Resource r = (Resource) node.as(Resource.class);
			StmtIterator it  = r.listProperties(rdfvalue);
			if(it.hasNext()) {
				lit = (Literal) it.nextStatement().getObject().as(Literal.class);
				if(lit == null ) {
					System.out.println(r.getURI());
				}
				sLit = lit.getString();
				//Find modifying predicates
				StmtIterator itmod = r.listProperties();
				while(itmod.hasNext()) {
					Statement stmt = itmod.nextStatement();
					if(!stmt.getPredicate().getURI().equals(rdfvalue.getURI())) {
						sLit += " (" + stmt.getPredicate().getLocalName()+" : ";
						RDFNode object = stmt.getObject();
						if(object.isResource()) {
							Resource res = (Resource) object.as(Resource.class);
							String uri = res.getURI();
							if(uri2tree.containsKey(uri)) {
								sLit += "<a href=\" " + uri +"\">" + notationorlabel(res) + "</a>";

							} else {
								sLit += uri;
							}
						} else if (object.isLiteral()) {
							Literal litobj = (Literal) object.as(Literal.class);
							sLit += litobj.getString();
						}
						sLit += ")";
					}
				}
			}
		}

		return sLit;
	}

	/********************************************************************************
	 *   Starting here: Methods to produce HTML output for a concept
	 *   
	 *   ******************************************************************************/

	private String getLabelsHTML(Resource concept) {
		String html = "";
		Map<String,String> prefLabs = getPrefLabels(concept); 
		Map<String,SortedSet<String>> altLabs =  getAltLabels(concept); 
		

	
		Set<String> Languages = new HashSet<String>();
		Languages.addAll(prefLabs.keySet());
		Languages.addAll(altLabs.keySet());
		boolean empty = true;
		
		for(String lang: Languages) {
			html += "<img src= \"" + imagepath + lang + ".png\" alt= \"" + lang + ".png\" alt= \"" + lang + "\">";
			String prefLab =  prefLabs.get(lang);
			if(prefLab != null) {
				html +=	" <b>" + prefLab + "</b>";
				empty = false;
			}
			SortedSet<String> sortedlabels = altLabs.get(lang);
			if(sortedlabels != null) {
				for(String alt:sortedlabels) {
					if(!empty) {
						html += ", ";
					}
					html += "<em>" + alt + "</em>";
					empty = false;
				}
			}
			html += "<br/>";
		}
	
		return html;
	}

	
	private String getNarrowerHTML(Resource c) {
		String html = "";
		
		ArrayList<Resource> concepts = getNarrowerConcepts(c);
		
		if(concepts != null && concepts.size() > 0) {
			html += "<br/>\n";
			html += "<b>";
			html += "Narrower: ";
			html += "</b>\n";
			for(Resource concept:concepts) {
				html += "<a href=\" " + concept.getURI() +"\">" + notationorlabel(concept) + "</a>; \n";
			}
			html = html.substring(0, html.length()-3); //remove the last semicolon
		}

		return html;
	}

	private String getBroaderHTML(Resource c) {
		String html = "";
		
		ArrayList<Resource> concepts = getBroaderConcepts(c);
		
		if(concepts != null && concepts.size() > 0) {
			html += "<br/>\n";
			html += "<b>";
			html += "Broader: ";
			html += "</b>\n";
			for(Resource concept:concepts) {
				html += "<a href=\" " + concept.getURI() +"\">" + notationorlabel(concept) + "</a>; \n";
			}
			html = html.substring(0, html.length()-3); //remove the last semicolon
		}

		return html;
	}
	
	
	private String getRelatedHTML(Resource c) {
		String html = "";
		
		ArrayList<Resource> concepts = getRelatedConcepts(c);
		
		if(concepts != null && concepts.size() > 0) {
			html += "<br/>\n";
			html += "<b>";
			html += "Related: ";
			html += "</b>\n";
			for(Resource concept:concepts) {
				html += "<a href=\" " + concept.getURI() +"\">" + notationorlabel(concept) + "</a>; \n";
			}
			html = html.substring(0, html.length()-3); //remove the last semicolon
		}

		return html;
	}
	
	
	private String getTopConceptHTML(Resource c) {
		String html = "";
		
		ArrayList<Resource> concepts = getTopConceptsSchemes(c);
		
		if(concepts != null && concepts.size() > 0) {
			html += "<br/>\n";
			html += "<b>";
			html += "Top Concept of Scheme: ";
			html += "</b>\n";
			for(Resource concept:concepts) {
				html += "<a href=\" " + concept.getURI() +"\">" + notationorlabel(concept) + "</a>; \n";
			}
			html = html.substring(0, html.length()-3); //remove the last semicolon
		}

		return html;
	}

	private String getOtherHTML(Resource c) {
		String html = "";
		
		html += "<hr/>\n";
		html += "<h3>";
		html += "Other Properties";
		html += "</h3>";
		
		html += "<table style=\"width:100%\">";
		
		StmtIterator propit = c.listProperties();
		int nrOfStmts = 0;
		while(propit.hasNext()) {
			Statement st = propit.nextStatement();

			boolean displayedProperty = false;

			for(Property p:standardProperties) {
				if(st.getPredicate().getURI().equals(p.getURI())) {
					displayedProperty = true;
					break;
				} 
				Property sub = ontology.getProperty(rdfs+"subPropertyOf");
				if(ontology.contains(ontology.createStatement(p,sub,st.getPredicate()))) {
					displayedProperty = true;
					break;
				} 
				if(ontology.contains(ontology.createStatement(st.getPredicate(),sub,p))) {
					displayedProperty = true;
					break;
				} 
				
			}
			if(displayedProperty) {
				continue;
			}
			
			String strPred ="";
			Resource resPredicate = (Resource) st.getPredicate().as(Resource.class);
			Statement predLabelStmt = resPredicate.getProperty(rdfslabel);
			if(predLabelStmt != null) {
				strPred = ((Literal) predLabelStmt.getObject().as(Literal.class)).getString();
			} else {
				String ns = resPredicate.getNameSpace();
				String pref = ontology.getNsURIPrefix(ns);
				if(pref != null && !pref.isEmpty()) {
					strPred += pref + ":" ;
				}
				strPred+= resPredicate.getLocalName();
			} 

			html += "<tr valign=\"top\"><td>";
			html += strPred;
			html += ":</td><td>";
			RDFNode object = st.getObject();
					
			if(object.isAnon() && object.canAs(RDFList.class)) {
				RDFList rdflist = (RDFList) object.as(RDFList.class);
				for(RDFNode member: (List<RDFNode>) rdflist.asJavaList()) { 
					if(member.isLiteral()) {
						Literal lit = (Literal) object.as(Literal.class);
						String lang = lit.getLanguage();
						if(lang != null && !lang.isEmpty()) {
							html += "<img src= \"" + imagepath + lang + ".png\" alt= \"" + lang + ".png\" alt= \"" + lang + "\">";
						}
						html+=lit.getString();
						nrOfStmts++;
					} else if(member.isResource()) {
						Resource res = (Resource) member.as(Resource.class);
						String uri = res.getURI();
						if(uri2tree.containsKey(uri)) {
							html += "<a href=\" " + uri +"\">" + notationorlabel(res) + "</a>";
						} else {
							html += uri;
						}
						nrOfStmts++;
					}
					html+= " <nbsp/>";
				}
			} else if(object.isResource()) {
				Resource res = (Resource) object.as(Resource.class);
				String uri = res.getURI();
				if(uri2tree.containsKey(uri)) {
					html += "<a href=\" " + uri +"\">" + notationorlabel(res) + "</a>";
				} else {
					String objectLabel = notationorlabel((Resource) object.as(Resource.class));
					html+= objectLabel;
				}
					
				nrOfStmts++;
			} else if(object.isLiteral()) {
				Literal lit = (Literal) object.as(Literal.class);
				String lang = lit.getLanguage();
				if(lang != null && !lang.isEmpty()) {
					html += "<img src= \"" + imagepath + lang + ".png\" alt= \"" + lang + ".png\" alt= \"" + lang + "\">";
				}
				html+=lit.getString();
				nrOfStmts++;
			}
			html += "</td></tr>";


		}
		html += "</table>"; 
		
		if(nrOfStmts > 0) {
			return html;
		} else {
			return "";
		}
	}
	
	private void DisplayResource(Browser conceptBrowser, Resource c) {
		
		String html = "<html><body>";
		html += "<h2>" + notationorlabel(c) + "</h2>\n";	
		
		html += "<p/>\n";
		String Notes = getNotes(c);
		html+= Notes;
		//TODO skos:changeNote, skos:definition, skos:editorialNote, skos:example, skos:historyNote and skos:scopeNote are each sub-properties of skos:note.

		String Notation = getNotation(c);
		if(Notation != null) {
			if(Notes != null && !Notes.isEmpty()) {
				html+="<br/>\n";
			}
			html+= "<b>Notation: </b><nbsp/>" + Notation +"\n";
		}
		html += "<br/>\n";
		html += "<b>URI: </b><nbsp/>" + c.getURI() +"\n";	
		html += "<hr/>\n";

		html += getLabelsHTML(c);
		html += "<p/>\n";
		html += getTopConceptHTML(c);
		html += getBroaderHTML(c);
		html += getNarrowerHTML(c);
		html += getRelatedHTML(c);
		html += getOtherHTML(c);

		html += "\n</body></html>";
		
		conceptBrowser.setText(html);
		
	}

	
	public void displayFile(Browser conceptBrowser) {
		
		File file = new File(filePath);
		
		String html = "<html><body>";
		html += "<h2>" + file.getName() + "</h2>\n";	
		
		html+= "<b>Path: </b><nbsp/>" + file.getAbsolutePath() +"\n";
		if(!file.canWrite()) {
			html+= "<br/>\n<b>Size: </b><nbsp/><br/>\n";
		}
		html+= "<br/>\n<b>Size: </b><nbsp/>" + file.length()/1024 +" kB\n";
		html+= "<br/>\n<b>Last modified: </b><nbsp/>" + new Date(file.lastModified()).toString() +"\n";
		html += "\n</body></html>";
		
		conceptBrowser.setText(html);
		
	}
	
	/**********************************************************************
	 * 
	 * Dislaying source
	 * 
	 **********************************************************************/
	
	private void DisplaySource(Text sourceViewer, Resource res) {
		
		Model localmodel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		
		PrefixMapping prefixes = PrefixMapping.Factory.create();
		//prefixes.setNsPrefixes(ontology.getNsPrefixMap());
		//TODO only used nsprefixes should be printed
		prefixes.setNsPrefix("skos", skos);
		prefixes.setNsPrefix("rdf", rdf);
		prefixes.setNsPrefix("rdfs", rdfs);
		prefixes.setNsPrefix("skosxl", skosxl);
		prefixes.setNsPrefix("dc", dc);
		prefixes.setNsPrefix("dcterms", dcterms);
		localmodel.withDefaultMappings(prefixes);
		StmtIterator it = res.listProperties();
		
		
		while(it.hasNext()) {
			Statement s = it.nextStatement();

			if(ontology.getRawModel().contains(s)) {
				localmodel.add(s);
				RDFNode objectnode = s.getObject();

				if(objectnode.isAnon()) {

					if(objectnode.canAs(RDFList.class))  {
						boolean hasRest = true;
						while(hasRest) {
							hasRest = false;
							StmtIterator subit = ((Resource) objectnode.as(Resource.class)).listProperties();
							while(subit.hasNext()) {
								Statement s1 = subit.nextStatement();
								localmodel.add(s1);
								RDFNode objectnode2 = s1.getObject();
								if(objectnode2.isAnon() && objectnode2.canAs(RDFList.class)) {
									objectnode = objectnode2;
									hasRest = true;
								}

							}

						} 

					}

					StmtIterator subit = ((Resource) objectnode.as(Resource.class)).listProperties();
					while(subit.hasNext()) {
						Statement s1 = subit.nextStatement();
						if(ontology.getRawModel().contains(s1)) {
							localmodel.add(s1);
						}
					}
				}
			}
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		localmodel.write(out,"TURTLE");
		try {
			sourceViewer.selectAll();
			sourceViewer.clearSelection();
			String strSource = new String(out.toByteArray(), "UTF8");
			sourceViewer.setText(strSource);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/**********************************************************************
	 * 
	 * Methods for navigation and synchronization
	 * 
	 **********************************************************************/
	
	public void selectURI(String uri, Browser conceptBrowser, Text sourceViewer) {
		TreeItem ti = uri2tree.get(uri);
		if(ti != null) {
			hierarchy.select(ti);
			TreeItem parent = ti.getParentItem();
			if(parent != null) {
				hierarchy.setTopItem(parent); 
			} else {
				hierarchy.setTopItem(ti); 
			}
			displayConcept((Resource) ti.getData(), conceptBrowser, sourceViewer);
		}
		
	}

	public void displayConcept(Resource c, Browser conceptBrowser, Text sourceViewer) {
		DisplayResource(conceptBrowser, c);
		DisplaySource(sourceViewer, c);	
	}
	


	public Map<Locale, Index> buildIndex() {
		Map<Locale, Index> indices = new HashMap<Locale, Index>();
		ResIterator conceptit = ontology.listSubjectsWithProperty(rdftype, skos_concept);
		while(conceptit.hasNext()) {
			Resource concept = conceptit.nextResource();
			
			
			Map<String, String> preflabels = getPrefLabels(concept);
			for(Entry<String,String> preflabel:preflabels.entrySet()) {
				String lang = preflabel.getKey();
				if(lang == null || lang.isEmpty()) {
					lang = "en";
				}
				Locale loc = new Locale(lang);
				Index lindex = (Index) indices.get(loc);
				if(lindex == null) {
					//lindex = new Trie();
					lindex = new TrigramIndex();
					indices.put(loc, lindex);
				}
				String[] labelwords = preflabel.getValue().split("[ _-]");
				for(String s:labelwords) {
					if(!s.isEmpty() && (labelwords.length == 1 || s.length() > 3)) { 
						//words up to 3 characters are stop words and are not indexed unless the stopword is the label of the concept
						//We e.g. exclude 'and' in 'transport and industry'
						lindex.add(s,new ConceptInfo(preflabel.getValue(),"prefLabel",concept.getURI(),1d/(1+Math.log(labelwords.length))));
					}
				}
			}
			
			Map<String, SortedSet<String>> altlabels = getAltLabels(concept);
			for(Entry<String, SortedSet<String>> altlabelset:altlabels.entrySet()) {
				String lang = altlabelset.getKey();
				if(lang == null || lang.isEmpty()) {
					lang = "en";
				}
				Locale loc = new Locale(lang);
				Index lindex = (Index) indices.get(loc);
				if(lindex == null) {
					//lindex = new Trie();
					lindex = new TrigramIndex();
					indices.put(loc, lindex);
				}
				for(String altlabel:altlabelset.getValue()) {
					String[] labelwords = altlabel.split(" ");
					for(String s:labelwords) {
						if(!s.isEmpty() && (labelwords.length == 1 || s.length() > 3)) { 
							//words up to 3 characters are stop words and are not indexed unless the stopword is the label of the concept
							//We e.g. exclude 'and' in 'transport and industry'
							lindex.add(s,new ConceptInfo(altlabel,"altLabel",concept.getURI(),1d/(1+Math.log(labelwords.length))));
						}
					}
				}
			}
		}
		return indices;
	}

	public String getLabel(String uri) {
		Resource indiv = ontology.getResource(uri);	
		
		return concept2notation.get(indiv);
	}

	

	
	
	

}
