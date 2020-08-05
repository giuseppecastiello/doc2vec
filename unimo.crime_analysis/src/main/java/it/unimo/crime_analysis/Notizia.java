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
			if ((in.charAt(i) >= '!' && in.charAt(i) <= '/')
					|| (in.charAt(i) >= ':' && in.charAt(i) <= '?' + 1)
					|| (in.charAt(i) >= '{' && in.charAt(i) <= '}' + 1)
					|| (in.charAt(i) == '�' || in.charAt(i) == '�')
					|| (in.charAt(i) == 174 || in.charAt(i) == 175 || in.charAt(i) == '�')) {
				if (i > 0 && in.charAt(i - 1) != ' ')
					out.append(' ');
				out.append(in.charAt(i));
				//out.append('\n');
				if (i < in.length() - 1 && in.charAt(i + 1) != ' ')
					out.append(' ');
			}
			else {
				out.append(in.charAt(i));
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
		return title + '\n' + description + '\n' + text + "\n\n";
	}
	
}
