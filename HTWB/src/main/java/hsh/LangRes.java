package hsh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/**
 * Multi-language support for this software (Currently supported: English, German, Dutch)
 *
 * @author  Christian Wartena
 */
public class LangRes {

	private static Map<Locale,Map<String,String>> data;
	
	static {
        Map<Locale,Map<String,String>> temp = new HashMap<Locale,Map<String,String>>();

        Map<String,String> de = new HashMap<String,String>();
        de.put("main.title", "Skosy (Developer Preview)");
        de.put("main.file", "Datei");
        de.put("main.open", "�ffnen");
        de.put("main.save", "Speichern");
        de.put("main.close", "Beenden");
        de.put("main.prefs", "Einstellungen");
        de.put("main.help", "Hilfe");
        de.put("main.about", "Impressum");
        de.put("main.hierar", "Hierarchie");
        de.put("main.search", "Suche");
        de.put("main.concept", "Konzeptansicht");
        de.put("main.source", "Quellansicht");
        de.put("search.search", "Suche");
        de.put("search.exact", "Exakte Suche");
        de.put("search.fuzzy", "Fuzzy Suche");
        de.put("search.srcLang", "Suchsprache");
        de.put("status.reading", "Lese");
        de.put("status.buildtree", "Erstelle Hierarchie ...");
        de.put("status.buildindex", "Erstelle Suchindex ...");
        de.put("status.addstatements", "Erg�nze fehlende Aussagen  ...");
        de.put("search.any", "alle");
        de.put("error.unknownext", "Unbekannte Extension");
        temp.put(new Locale("de"), de);
        Map<String,String> en = new HashMap<String,String>();
        en.put("main.title", "Skosy (Developer Preview)");
        en.put("main.file", "File");
        en.put("main.open", "Open");
        en.put("main.save", "Save");
        en.put("main.close", "Quit");
        en.put("main.prefs", "Preferences");
        en.put("main.help", "Help");
        en.put("main.about", "About HTWB");
        en.put("main.hierar", "Hierarchy");
        en.put("main.search", "Search");
        en.put("main.concept", "Concept view");
        en.put("main.source", "Source view");
        en.put("search.search", "Search");
        en.put("search.exact", "Exact search");
        en.put("search.fuzzy", "Fuzzy search");
        en.put("search.srcLang", "Search language:");
        en.put("status.reading", "Reading");
        en.put("status.buildtree", "Building hierarchy ...");
        en.put("status.buildindex", "Building search index ...");
        en.put("status.addstatements", "Adding missing statements ...");
        en.put("search.any", "any");
        en.put("error.unknownext", "Unknown extension");
        temp.put(new Locale("en"), en);
        Map<String,String> nl = new HashMap<String,String>();
        nl.put("main.title", "Skosy (Developer Preview)");
        nl.put("main.file", "File");
        nl.put("main.open", "Openen");
        nl.put("main.save", "Opslaan");
        nl.put("main.close", "Afsluiten");
        nl.put("main.prefs", "Opties");
        nl.put("main.help", "Help");
        nl.put("main.about", "Over de HTWB");
        nl.put("main.hierar", "Hi�rarchie");
        nl.put("main.search", "Zoeken");
        nl.put("main.concept", "Concept view");
        nl.put("main.source", "Source view");
        nl.put("search.search", "Zoeken");
        nl.put("search.exact", "Exakt zoeken");
        nl.put("search.fuzzy", "Fuzzy zoeken");
        nl.put("search.srcLang", "Zoektalen:");
        nl.put("status.reading", "Lezen van");
        nl.put("status.buildtree", "Hierarchy wordt opgebouwd ...");
        nl.put("status.buildindex", "Zoekindex wordt opgebouwd ...");
        nl.put("status.addstatements", "Ontbrekende proposities worden teoegevoegd ...");
        nl.put("search.any", "alle");
        nl.put("error.unknownext", "Onbekende extensie");
        temp.put(new Locale("nl"), nl);
        data = Collections.unmodifiableMap(temp);
    }

	public static String getStr(Locale spr,String key) {
		Map<String,String> lr = data.get(spr);
		String str = lr.get(key);
		if(str == null) {
			str = "";
		} 
		return str;
	}

	public static boolean supports(Locale spr) {
		return data.containsKey(spr);

	}
}
