package org.mobicents.ussdgateway;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

/**
 * @author amit bhayani
 * 
 */
public class EventsSerializeFactory {

	// Creates some useful aliases for class names.
	private final XMLBinding binding = new XMLBinding();

	private final XMLObjectWriter writer;
	private final ByteArrayOutputStream baos;
	
	private final XMLObjectReader reader;
	

	public EventsSerializeFactory() throws XMLStreamException {
		this.binding.setAlias(Dialog.class, "dialog");
		this.binding.setClassAttribute("type");
		this.baos = new ByteArrayOutputStream();
		
		this.writer = new XMLObjectWriter();
		
		this.reader = new XMLObjectReader();
	}
	
	private void resetWriter() throws XMLStreamException{
		this.writer.reset();
		this.baos.reset();
		this.writer.setBinding(binding); 
		this.writer.setIndentation("\t"); 
		this.writer.setOutput(this.baos);
	}

	public byte[] serialize(Dialog dialog) throws XMLStreamException {
		this.resetWriter();
		
		this.writer.write(dialog, "dialog", Dialog.class);
		this.writer.flush();
		byte[] data = baos.toByteArray();
		
		return data;
	}
	
	public Dialog deserialize(byte[] data) throws XMLStreamException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		this.reader.setInput(bais);
		Dialog dialog = this.reader.read("dialog", Dialog.class);
		this.reader.reset();
		return dialog;
	}
	
	public Dialog deserialize(InputStream is) throws XMLStreamException {
		this.reader.setInput(is);
		Dialog dialog = this.reader.read("dialog", Dialog.class);
		this.reader.reset();
		return dialog;
	}
}
