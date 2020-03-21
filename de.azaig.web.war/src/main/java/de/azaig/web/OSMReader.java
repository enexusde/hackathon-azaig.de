package de.azaig.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OSMReader {
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		BZip2CompressorInputStream bic = new BZip2CompressorInputStream(new FileInputStream(new File("X:\\Medien\\Karten\\planet-200309.osm.bz2")), true);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.newSAXParser().parse(bic, new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				System.out.println(qName);
			}
		});
		bic.close();
	}
}