package AudioPrediction;

import java.util.Vector;

public class BufferVector extends Vector<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6786534258286663894L;
	int nbOfElements = 0;
	
	BufferVector(int nbOfElements, int initialValue) {
		super(nbOfElements);
		this.nbOfElements = nbOfElements;
		for (int i = 0; i < nbOfElements; i++) {
			this.add(initialValue);
		}
	}
	
	public void push (Integer element) {
		this.add(0, element);
		this.remove(nbOfElements);
	}
	
	public int getSize() {
		return nbOfElements;
	}
}
