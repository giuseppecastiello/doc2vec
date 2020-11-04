package it.unimo.crime_analysis;

import java.sql.Date;
import java.util.Calendar;

import javax.annotation.Nonnull;

public class Notice {
	private String all, tag, municipality;
	private Date date, date_event;
	private int id;

	public Notice(String title, String description, String text, Date date, String tag, String municipality, Date date_event, int id) {
		super();
		this.all = addSpaces(title + " " + description + " " + text);
		this.date = date;
		this.tag = tag.toLowerCase().strip();
		this.municipality = municipality.toLowerCase().strip();
		this.date_event = date_event;
		this.id = id;
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
	
	public String gsToString() {
		return id + "  " + tag + "\t" + municipality + "\n" + all;
	}

	@Override
	public String toString() {
		return all;
	}

	public boolean isInWindowWith(Notice other) {
		if (Math.abs(Math.round((this.date.getTime() - other.date.getTime()) / 86400000.0)) <= 3)
			return true;
		if ((this.date_event != null && other.date_event != null) &&
				(Math.abs(Math.round((this.date_event.getTime() - other.date_event.getTime()) / 86400000.0)) <= 3))
			return true;
		return false;
	}

	public boolean borderLineCompare(Notice other, double similarity, double threshold) {
		if (!this.municipality.equals("") && this.municipality.equals(other.municipality)) {
			similarity += 0.25;
		}
		if (!this.tag.equals("") && this.tag.equals(other.tag)) {
			similarity += 0.25;
		}
		return similarity >= threshold;
	}

	@SuppressWarnings("deprecation")
	public boolean isInGen2020() {
		//System.out.println(this.date + " = " + this.date.getYear() + " / " + this.date.getMonth());
		if (this.date.getYear() == 120 && this.date.getMonth() == 0)
			return true;
		return false;
	}
}
