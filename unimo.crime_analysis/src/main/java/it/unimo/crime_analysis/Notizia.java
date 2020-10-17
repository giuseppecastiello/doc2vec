package it.unimo.crime_analysis;

import javax.annotation.Nonnull;

public class Notizia {
	String title;
	String description;
	String text;
	
	public Notizia(String title, String description, String text) {
		super();
		this.title = title;
		this.description = description;
		this.text = text;
	}
	
	public String addSpaces(@Nonnull String in) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (!(c >= 'a' && c <= 'z') &&
					!(c >= 'A' && c <= 'Z') &&
					!(c >= '0' && c<= '9') &&
					!(c >= 192 && c<= 382) &&
					!(c == ' ')) {
				if (i > 0 && in.charAt(i - 1) != ' ')
					out.append(' ');
				out.append(c);
				if (i < in.length() - 1 && in.charAt(i + 1) != ' ')
					out.append(' ');
			}
			else {
				if (c != '\n' && c != '\r')
					out.append(c);
			}
		}
		return out.toString();
	}
	
	public Notizia format() {
		this.title = addSpaces(this.title);
		this.description = addSpaces(this.description);
		this.text = addSpaces(this.text);
		return this;
	}
	
	@Override
	public String toString() {
		return title + '\n' + description + '\n' + text + "\n";
	}
	
}
