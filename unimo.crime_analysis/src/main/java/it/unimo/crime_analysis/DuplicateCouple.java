package it.unimo.crime_analysis;

public class DuplicateCouple {
	int id1, id2;
	
	public DuplicateCouple(int id1, int id2) {
		if (id1 < id2) {
			this.id1 = id1;
			this.id2 = id2;
		}
		else {
			this.id1 = id2;
			this.id2 = id1;			
		}
	}
	
	@Override
	public boolean equals(Object o) {
		DuplicateCouple other = (DuplicateCouple) o;
		if (this.id1 == other.id1 && this.id2 == other.id2)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return id1 + "\t" + id2;
	}
	
	
}
