package il.ac.tau.cs.software1.inventory;

/*
 * A simple array-based Inventory (Question 6).
 */
public class NaiveInventory implements Inventory {

	private static final int INITIAL_CAPACITY = 100;
	private Collectible[] collArr;
	private int numOfItems;

	public NaiveInventory() {
		this.collArr = new Collectible[INITIAL_CAPACITY];
        this.numOfItems = 0;
		}

	@Override
	public void collect(Collectible collectible) {
		if (this.numOfItems == this.collArr.length) {
			Collectible[] newArr = new Collectible[this.collArr.length * 2];
			for (int i = 0; i < this.numOfItems; i++) {
				newArr[i] = this.collArr[i];
			}
			this.collArr = newArr;
		}   
		this.collArr[this.numOfItems] = collectible;
		this.numOfItems++;
	}

	@Override
	public float getTotalWeight() {
		float totalWeight = 0.0f;
		for (int i = 0; i < this.numOfItems; i++) {
			totalWeight += this.collArr[i].getWeight();
		}
		return totalWeight;
	}

	@Override
	public int getCurrentCount() {
		return this.numOfItems;
	}

	// Returns the array of Collectibles that is stored in the field of this class
	public Collectible[] getCollArr() {
		return this.collArr;
	}
}
