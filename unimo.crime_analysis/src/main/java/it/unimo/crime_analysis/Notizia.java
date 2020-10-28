package it.unimo.crime_analysis;

import javax.annotation.Nonnull;

public class Notizia {
	String all;
	
	public Notizia(String all) {
		super();
		this.all = all;
	}
	
	public Notizia(String title, String description, String text) {
		super();
		this.all = addSpaces(title + " " + description + " " + text);
	}
	
	private String addSpaces(@Nonnull String in) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (!(c >= 'a' && c <= 'z') &&
					!(c >= 'A' && c <= 'Z') &&
					!(c >= '0' && c<= '9') &&
					!(c >= 192 && c<= 382) &&
					!(c == ' ')) {
				if (c != '\n' && c != '\r') {
					if (i > 0 && in.charAt(i - 1) != ' ')
						out.append(' ');
					out.append(c);
					if (i < in.length() - 1 && in.charAt(i + 1) != ' ')
						out.append(' ');
				}
			}
			else {
				out.append(c);
			}
		}
		return out.toString();
	}
	
	@Override
	public String toString() {
		return all + "\n";
	}
	
}
